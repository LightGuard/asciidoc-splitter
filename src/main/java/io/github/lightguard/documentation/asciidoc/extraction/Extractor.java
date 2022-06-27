package io.github.lightguard.documentation.asciidoc.extraction;

import io.github.lightguard.documentation.asciidoc.Util;
import io.github.lightguard.documentation.asciidoc.cli.Issue;
import io.github.lightguard.documentation.asciidoc.extension.ReaderPreprocessor;
import io.github.lightguard.documentation.asciidoc.extension.ReplaceWithTreeProcessor;
import io.github.lightguard.documentation.asciidoc.extraction.model.Assembly;
import io.github.lightguard.documentation.asciidoc.extraction.model.ExtractedModule;
import io.github.lightguard.documentation.asciidoc.extraction.model.Task;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.ast.Document;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.*;
import java.util.*;
import java.util.logging.LogManager;
import java.util.logging.Logger;

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

    /**
     * Process the source.
     *
     * @return exit code
     */
    public int process() {
        logger.fine("Starting up Asciidoctor");
        var preprocessor = new ReaderPreprocessor();
        var replaceWithProcessor = new ReplaceWithTreeProcessor();

        OptionsBuilder optionsBuilder = OptionsBuilder.options();
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        asciidoctor.javaExtensionRegistry()
                .preprocessor(preprocessor)
                .treeprocessor(replaceWithProcessor);

        replaceWithProcessor.setReaderPreprocessor(preprocessor);

        // We need access to the line numbers and source
        optionsBuilder.sourcemap(true);
        optionsBuilder.attributes(AttributesBuilder.attributes().attributes(task.getAttributes()));

        final Path sourceDirPath = this.task.getLocation().getDirectoryPath().normalize();
        final Path targetDirPath = this.task.getPushableLocation().getDirectoryPath().normalize();

        try {
            var walker = new AsciidocChapFileVisitor(task.getIgnoreFiles());
            Files.walkFileTree(sourceDirPath, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, walker);

            this.logger.info("Processing files: " + walker.getAdocFiles());

            for (File file : walker.getAdocFiles()) {
                // We only want to process chap files, others should be moved to modules.
                if (!file.getName().startsWith("chap-") && !file.getName().startsWith("assembly-")) {
                    try {
                        this.logger.fine("Copying non chap- file '" + file + "' to modules directory");
                        Path modulesDir = Files.createDirectories(targetDirPath.resolve("modules"));
                        Files.copy(file.toPath(), modulesDir.resolve(file.getName()),
                                StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        this.logger.severe("Could not move non chapter file: " + e.getMessage());
                    }
                    continue;
                }

                logger.fine("Loading file '" + file.getAbsolutePath() + "' into asciidoctor");
                var doc = asciidoctor.loadFile(file, optionsBuilder.asMap());
                var loc = Paths.get(doc.getSourceLocation().getDir()).getFileName();
                doc.setAttribute("splitter-doc-root", loc, true);
                var lines = preprocessor.getLines();

                findSections(doc, lines, preprocessor.getAssemblyBody());

                writeModules(targetDirPath);
                writeAssemblies(targetDirPath);
            }

            // Create the _images and _artifacts directories
            createAndCopyDir(sourceDirPath.resolve("_artifacts"), targetDirPath);
            createAndCopyDir(sourceDirPath.resolve("_images"), targetDirPath);

            // Create and setup titles-enterprise folder, if necessary
            if (!Files.exists(targetDirPath.resolve(TITLES_ENTERPRISE))) {
                logger.info("Copying files from " + TITLES_ENTERPRISE);
                moveTitles(sourceDirPath.resolve(TITLES_ENTERPRISE), targetDirPath);
            } else {
                logger.info(TITLES_ENTERPRISE + " exists in output already, ignoring copy");
            }

            // create symlinks in assemblies
            createAssemblySymlinks(sourceDirPath, targetDirPath);
        } catch (IOException e) {
            this.logger.severe(e.getMessage());
        }

        long errors = this.issues.stream().filter(Issue::isError).count();

        try {
            this.task.getPushableLocation().close();
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
        }
        this.logger.warning("Found " + this.issues.size() + " issues. " + errors + " Errors.");

        // We want to tell the script that there were errors and not to push
        if (errors > 0)
            return -1;

        return 0;
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
                Files.createSymbolicLink(assembliesDir.resolve("modules"),
                        assembliesDir.relativize(targetDirPath.resolve("modules")));
        } catch (FileAlreadyExistsException e) {
            this.logger.info("Symlink 'modules' already exists, continuing, please verify.");
        } catch (IOException e) {
            this.logger.severe("Failed creating symlink: " + e.getMessage());
        }

        try {
            if (Files.exists(sourceDirPath.resolve("_artifacts")))
                Files.createSymbolicLink(assembliesDir.resolve("_artifacts"),
                        assembliesDir.relativize(targetDirPath.resolve("_artifacts")));
        } catch (FileAlreadyExistsException e) {
            this.logger.info("Symlink '_artifacts' already exists, continuing, please verify.");
        } catch (IOException e) {
            this.logger.severe("Failed creating symlink: " + e.getMessage());
        }

        try {
            if (Files.exists(sourceDirPath.resolve("_images"))) {
                Files.createSymbolicLink(assembliesDir.resolve("_images"),
                        assembliesDir.relativize(targetDirPath.resolve("_images")));

                // Add it for modules too
                var modulesDir = assembliesDir.resolve("../modules");
                Files.createSymbolicLink(modulesDir.resolve("_images"),
                        modulesDir.relativize(targetDirPath.resolve("_images")));
            }
        } catch (FileAlreadyExistsException e) {
            this.logger.info("Symlink '_images' already exists, continuing, please verify.");
        } catch (IOException e) {
            this.logger.severe("Failed creating symlink: " + e.getMessage());
        }
    }

    /**
     * returns true if all went well. false if there was some problem with
     * uniqueness.
     */
    private void findSections(Document doc, List<String> lines, StringBuilder processedBody) {
        var assembly = new Assembly(doc, lines, processedBody);
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
        this.assemblies.forEach(a -> {
            try {
                // Create any directories that need to be created
                Path assembliesDir = Files
                        .createDirectories(outputDirectory.resolve("assemblies"));

                if (a.shouldCreateAssembly()) {
                    var outputFile = assembliesDir.resolve(a.getFilename());
                    logger.fine("Writing assembly file: " + outputFile);
                    try (Writer output = new FileWriter(outputFile.toFile())) {
                        // TODO: We could search the source for parent-context and add if necessary
                        //       Disabling for now.
                        if (task.isPv2())
                            output.append(Util.fixForPv2(a.getSource()));
                        else
                            output.append(Util.tweakSource(a.getSource()));
                    }
                }
            } catch (IOException e) {
                logger.severe("Error writing assembly (" + a + "): " + e.getMessage());
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
                            .append("[id='").append(module.getId()).append("_{context}']\n")
                            // Adding the section title
                            .append("= ").append(module.getSection().getTitle()).append("\n");
                    if (task.isPv2())
                        output.append(":imagesdir: ../_images\n");

                    output.append(module.getSource());
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
            if (e instanceof FileAlreadyExistsException) {
                logger.fine("Directory already exists, please verify output: " + ((FileAlreadyExistsException) e).getFile());
                return;
            }
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
                    new TitlesEnterpriseCopyTreeFileVisitor(sourceDir, targetDir));
        } catch (IOException e) {
            if (e instanceof FileAlreadyExistsException) {
                logger.fine("File already exists, please verify output: " + ((FileAlreadyExistsException) e).getFile());
                return;
            }
            addIssue(Issue.error(e.toString(), null));
        }
    }
}
