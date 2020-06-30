package com.redhat.documentation.asciidoc.extraction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.redhat.documentation.asciidoc.Util;
import com.redhat.documentation.asciidoc.cli.ExtractionRunner;
import com.redhat.documentation.asciidoc.cli.Issue;
import com.redhat.documentation.asciidoc.extraction.model.Task;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.ast.Document;
import org.asciidoctor.jruby.AsciiDocDirectoryWalker;

public class Extractor {
    public static final String TITLES_ENTERPRISE = "titles-enterprise";
    private final List<Assembly> assemblies;
    private final Set<ExtractedModule> modules;
    private final List<Issue> issues = new ArrayList<>();
    private final Task task;
    private final Logger logger;

    public Extractor(Task task) {
        this.task = task;
        this.assemblies = new ArrayList<>();
        this.modules = new HashSet<>();
        this.logger = LogManager.getLogManager().getLogger("");
    }

    public void process() {
        logger.fine("Starting up Asciidoctor");
        var preprocessor = new ReaderPreprocessor();

        OptionsBuilder optionsBuilder = OptionsBuilder.options();
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        asciidoctor.javaExtensionRegistry()
                        .preprocessor(preprocessor)
                        .treeprocessor(new ReplaceWithTreeProcessor());

        // We need access to the line numbers and source
        optionsBuilder.sourcemap(true);
        optionsBuilder.attributes(AttributesBuilder.attributes().attributes(task.getAttributes()));

        final Path sourceDirPath = this.task.getLocation().getDirectoryPath().normalize();
        final Path targetDirPath = this.task.getPushableLocation().getDirectoryPath().normalize();

        for (File file : new AsciiDocDirectoryWalker(sourceDirPath.toString())) {
            // Skip adoc files in the title enterprise directory
            if (file.getParent() != null && file.getParent().contains(TITLES_ENTERPRISE)) {
                continue;
            }

            logger.fine("Loading file '" + file.getAbsolutePath() + "' into asciidoctor");
            var doc = asciidoctor.loadFile(file, optionsBuilder.asMap());
            var lines = preprocessor.getLines();

            findSections(doc, lines);

            writeModules(targetDirPath);
            writeAssemblies(targetDirPath);
        }

        // Create the _images and _artifacts directories
        createAndCopyDir(sourceDirPath.resolve("_artifacts"), targetDirPath);
        createAndCopyDir(sourceDirPath.resolve("_images"), targetDirPath);

        // Create and setup titles-enterprise folder, if necessary
        moveTitles(sourceDirPath.resolve(TITLES_ENTERPRISE), targetDirPath);

        // create symlinks in assemblies
        createAssemblySymlinks(sourceDirPath, targetDirPath);

        // Push/Save the output
        this.logger.info("Pushing content (if applicable)");
        this.task.getPushableLocation().push();

        long errors = this.issues.stream().filter(Issue::isError).count();

        this.logger.warning("Found " + this.issues.size() + " issues. " + errors + " Errors.");
    }

    /**
     * Create symlinks in assemblies directory for ccutils to run correctly
     *
     * @param targetDirPath
     */
    private void createAssemblySymlinks(Path sourceDirPath, Path targetDirPath) {
        var assembliesDir = targetDirPath.resolve("assemblies");

        this.logger.fine("Creating symlinks in assembly directory");

        try {
            // Create symlinks for modules, _artifacts, and _images
            if (Files.notExists(assembliesDir.resolve("modules"))) // We only need this once
                Files.createSymbolicLink(assembliesDir.resolve("modules"), targetDirPath.resolve("modules"));

            if (Files.exists(sourceDirPath.resolve( "_artifacts")))
                Files.createSymbolicLink(assembliesDir.resolve("_artifacts"), sourceDirPath.resolve("_artifacts"));

            if (Files.exists(sourceDirPath.resolve("_images")))
                Files.createSymbolicLink(assembliesDir.resolve("_images"), sourceDirPath.resolve("_images"));
        } catch (IOException e) {
            this.logger.severe("Failed creating symlinks: " + e.getMessage());
        }
    }

    /**
     * returns true if all went well. false if there was some problem with
     * uniqueness.
     */
    private void findSections(Document doc, List<String> lines) {
        var assembly = new Assembly(doc, lines);
        logger.fine("Found assembly: " + assembly.toString());

        this.assemblies.add(assembly);
        for (var module : assembly.getModules()) {
            if (!this.modules.add(module)) {
                var duplicate = this.modules.stream().filter(m -> m.equals(module)).findFirst();
                addIssue(Issue.error("Module with non-unique id. " + module + " is a duplicate of " + duplicate, doc));
            }
        }
        // We should have all the assemblies, and all of their modules now
    }

    private void addIssue(Issue error) {
        this.logger.severe(error.toString());
        this.issues.add(error);
    }

    private void writeAssemblies(Path outputDirectory) {
        // Setup templates for modules
        String templateStart = getTemplateContents("templates/start.adoc");
        String templateEnd = getTemplateContents("templates/end.adoc");

        this.assemblies.forEach(a -> {
            try {
                // Create any directories that need to be created
                Path assembliesDir = Files
                        .createDirectories(outputDirectory.resolve("assemblies"));

                if (a.shouldCreateAssembly()) {
                    var outputFile = assembliesDir.resolve(a.getFilename());
                    logger.fine("Writting assembly file: " + outputFile);
                    try (Writer output = new FileWriter(outputFile.toFile())) {
                            output.append(templateStart)
                                    .append("\n")
                                    .append(Util.fixIncludes(a.getSource()))
                                    .append("\n")
                                    .append(templateEnd);
                    }
                }
            } catch (IOException e) {
                logger.severe("Error writting assembly (" + a + "): " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    private void writeModules(Path targetDirectory) {
        // Create the modules directory and write the files
        try {
            // Create the output directories
            Path modulesDir = Files.createDirectories(targetDirectory.resolve("modules"));
            Set<Path> visitedPaths = new HashSet<>();

            for (ExtractedModule module : this.modules) {
                // Create output file
                if (!module.getFolder().isEmpty()) {
                    Path topicFolder = Files.createDirectories(modulesDir.resolve(module.getFolder()));
                    Path moduleOutputFile = Paths.get(topicFolder.toString(), module.getFileName());

                    if (moduleOutputFile.toFile().exists()) {
                        if (visitedPaths.contains(moduleOutputFile)) {
                            this.logger.severe("Already written to this file: " + moduleOutputFile + " for " + module);
                            return;
                        }
                    } else {
                        moduleOutputFile = Files.createFile(moduleOutputFile);
                    }
                    visitedPaths.add(moduleOutputFile);

                    logger.fine("Writing module file: " + moduleOutputFile);

                    // Output the module
                    try (Writer output = new FileWriter(moduleOutputFile.toFile())) {
                        output
                                // Adding the id of the module
                                .append("[id=\"").append(module.getId()).append("_{context}\"]\n")
                                // Adding the section title
                                .append("= ").append(module.getSection().getTitle()).append("\n")
                                .append(Util.fixIncludes(module.getSource()));
                    }
                }
            }
        } catch (IOException e) {
            logger.severe("Error writing a module: " + e.getMessage());
        }
    }

    private void createAndCopyDir(Path sourceDir, Path targetDir) {
        try {
            if (sourceDir.toFile().exists()) {
                Files.walkFileTree(sourceDir, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
                        new CopyTreeFileVisitor(sourceDir, targetDir));
            }
        } catch (IOException e) {
            addIssue(Issue.error(e.toString(), null));
        }
    }

    /**
     * Moves all the files and folders under the titles-enterprise directory to the output
     *
     * @param sourceDir
     * @param targetDir
     */
    private void moveTitles(Path sourceDir, Path targetDir) {
        try {
            logger.fine("Moving files from the titles-enterprise directory");
            Files.walkFileTree(sourceDir, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
                    new TitleCopyTreeFileVisitor(targetDir, sourceDir));
        } catch (IOException e) {
            addIssue(Issue.error(e.toString(), null));
        }
    }

    private String getTemplateContents(String templateLocation) {
        final var cl = ExtractionRunner.class.getClassLoader();
        final var resource = cl.getResourceAsStream(templateLocation);
        assert resource != null;

        return new BufferedReader(new InputStreamReader(resource)).lines().collect(Collectors.joining("\n"));
    }
}
