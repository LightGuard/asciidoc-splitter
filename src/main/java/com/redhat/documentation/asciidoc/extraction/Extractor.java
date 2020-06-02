package com.redhat.documentation.asciidoc.extraction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.redhat.documentation.asciidoc.Util;
import com.redhat.documentation.asciidoc.cli.ExtractionRunner;
import com.redhat.documentation.asciidoc.cli.Issue;
import com.redhat.documentation.asciidoc.extraction.model.Task;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.ast.Document;
import org.asciidoctor.jruby.AsciiDocDirectoryWalker;

public class Extractor {
    public static final String TITLES_ENTERPRISE = "titles-enterprise";
    private final List<Assembly> assemblies;
    private final Set<ExtractedModule> modules;
    private final List<Issue> issues = new ArrayList<>();
    private final Task task;

    public Extractor(Task task) {
        this.task = task;
        assemblies = new ArrayList<>();
        modules = new HashSet<>();
    }

    public void process() {
        var preprocessor = new ReaderPreprocessor();

        OptionsBuilder optionsBuilder = OptionsBuilder.options();
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        asciidoctor.javaExtensionRegistry().preprocessor(preprocessor);

        // We need access to the line numbers and source
        optionsBuilder.sourcemap(true);

        final Path sourceDirPath = this.task.getLocation().getDirectoryPath();
        final Path targetDirPath = this.task.getPushableLocation().getDirectoryPath();

        for (File file : new AsciiDocDirectoryWalker(sourceDirPath.toString())) {
            var doc = asciidoctor.loadFile(file, optionsBuilder.asMap());
            var lines = preprocessor.getLines();

            findSections(doc, lines);

            writeModules(targetDirPath);
            writeAssemblies(targetDirPath);
        }

        // Move all the extra assets
        moveNonadoc(sourceDirPath, targetDirPath);

        // Create and setup titles-enterprise folder
        createTitlesDirectory(targetDirPath);
        moveTitles(Paths.get(sourceDirPath.toString(), "../" + TITLES_ENTERPRISE), targetDirPath);

        // create symlinks in assemblies
        createAssemblySymlinks(targetDirPath);

        // Push/Save the output
        this.task.getPushableLocation().push();

        long errors = this.issues.stream().filter(Issue::isError).count();

        System.out.println("Found " + this.issues.size() + " issues. " + errors + " Errors.");
    }

    /**
     * Create symlinks in assemblies directory for ccutils to run correctly
     * @param targetDirPath
     */
    private void createAssemblySymlinks(Path targetDirPath) {
        var assembliesDir = Path.of(targetDirPath.toString(), "assemblies").toString();

        try {
            // Create symlinks for modules, _artifacts, and _images
            if (Files.notExists(Paths.get(assembliesDir, "modules"))) // We only need this once
                Files.createSymbolicLink(Paths.get(assembliesDir, "modules"),
                        Path.of(targetDirPath.toString(), "modules"));

            if (Files.exists(Paths.get(assembliesDir, "..", "_artifacts")))
                Files.createSymbolicLink(Paths.get(assembliesDir, "_artifacts"),
                        Paths.get(assembliesDir,"..", "modules"));

            if (Files.exists(Paths.get(assembliesDir, "..", "_images")))
                Files.createSymbolicLink(Paths.get(assembliesDir, "_images"),
                        Paths.get(assembliesDir,"..", "modules"));
        } catch (IOException e) {
            System.err.println("Failed creating symlinks: " + e.getMessage());
        }
    }

    /**
     * returns true if all went well. false if there was some problem with
     * uniqueness.
     */
    private void findSections(Document doc, List<String> lines) {
        // TODO: This should probably be configurable

        var assembly = new Assembly(doc, lines);
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
        System.out.println(error);
        this.issues.add(error);
    }

    private void writeAssemblies(Path outputDirectory) {
        // Setup templates for modules
        String templateStart = getTemplateContents("templates/start.adoc");
        String templateEnd = getTemplateContents("templates/end.adoc");

        // TODO create modules, _artifacts, _images symlinks in this folder

        this.assemblies.forEach(a -> {
            try {
                // Create any directories that need to be created
                Path assembliesDir = Files
                        .createDirectories(outputDirectory.resolve("assemblies"));

                if (a.shouldCreateAssembly()) {
                    var outputFile = Paths.get(assembliesDir.toString(), a.getFilename());
                    try (Writer output = new FileWriter(outputFile.toFile())) {
                        output.append(templateStart).append("\n").append(a.getSource()).append("\n").append(templateEnd);
                    }
                }
            } catch (IOException e) {
                // TODO: We blew-up in an unexpected way, handle this
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
                            System.err.println("Already written to this file: " + moduleOutputFile + " for " + module);
                            return;
                        }
                    } else {
                        moduleOutputFile = Files.createFile(moduleOutputFile);
                    }
                    visitedPaths.add(moduleOutputFile);

                    // Output the module
                    try (Writer output = new FileWriter(moduleOutputFile.toFile())) {
                        output
                                // Adding the id of the module
                                .append("[id=\"").append(module.getId()).append("_{context}\"]\n")
                                // Adding the section title
                                .append("= ").append(module.getSection().getTitle()).append("\n")
                                .append(module.getSource());
                    }
                }
            }
        } catch (IOException e) {
            // TODO: We blew-up in an unexpected way, handle this
            throw new RuntimeException(e);
        }
    }

    private void moveNonadoc(Path sourceDir, Path targetDir) {
        try {
            var assetsDir = Files.createDirectories(targetDir.resolve(Util.ASSETS_LOCATION));
            var destinationDir = assetsDir.toFile().toPath();
            var adocExtRegex = Pattern.compile("^[^_.].*\\.a((sc(iidoc)?)|d(oc)?)$");


            // TODO: This should be another (specialized?) instance of the CopyTreeFileVisitor
            Files.walkFileTree(sourceDir, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
                    new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                                throws IOException {
                            var targetDir = destinationDir.resolve(sourceDir.relativize(dir));

                            // Create the directory structure in the new location
                            try {
                                Files.copy(dir, targetDir);
                            } catch (FileAlreadyExistsException e) {
                                if (!Files.isDirectory(targetDir))
                                    addIssue(Issue.error("Trying to create a non-directory: " + targetDir, null));
                            }

                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            // We are not an asciidoc file
                            if (!adocExtRegex.matcher(file.getFileName().toString()).matches()) {
                                Files.copy(file, destinationDir.resolve(sourceDir.relativize(file)));
                            }

                            return FileVisitResult.CONTINUE;
                        }
                    });

        } catch (IOException e) {
            addIssue(Issue.error(e.getMessage(), null));
        }
    }

    /**
     * Creates the titles-enterprise directory for the output
     *
     * @param parentDirectory
     */
    private void createTitlesDirectory(Path parentDirectory) {
        try {
            Files.createDirectory(Paths.get(parentDirectory.toString(), "titles-enterprise"));
        } catch (IOException e) {
            addIssue(Issue.error("Error creating directory 'titles-enterprise' in output folder: " + e.getMessage(),
                    null));
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
            var dirs = sourceDir.getName(0).toFile();

            Files.walkFileTree(sourceDir, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
                    new TitleCopyTreeFileVisitor(targetDir, sourceDir));

//            Path sourceD = Paths.get(dirs.toString(), "titles-enterprise");
//            Path titlesDir = Files.createDirectories(targetDir.resolve(sourceD.getFileName().toString())); // DONE
//            Path assemblies = Paths.get(targetDir.toString(), "assemblies").toAbsolutePath();
//            for (File file : titlesDir.toFile().listFiles()) {
//                if (file.isDirectory()) {
//                    for (File f : file.listFiles()) {
//                        if (f.isDirectory() && !Files.isSymbolicLink(f.toPath())) {
//                            f.delete();
//                        }
//                        Path titles_assemblies = Paths.get(f.getParent(), "assemblies");
//                        if (Files.exists(titles_assemblies)) {
//                            Files.delete(titles_assemblies);
//                        }
//                        Files.createSymbolicLink(titles_assemblies, assemblies);
//                        if (f.toString().endsWith(".adoc")) {
//                            Stream<String> lines = Files.lines(f.toPath());
//                            List<String> replaced = lines.map(line -> line.replaceAll("::(.*\\/)",
//                                    "::" + assemblies.toFile().getName() + "/assembly-")).collect(Collectors.toList());
//                            Files.write(f.toPath(), replaced);
//                            lines.close();
//                        }
//                    }
//                }
//            }

        } catch (IOException e) {
            addIssue(Issue.error(e.getMessage(), null));
        }
    }

    private String getTemplateContents(String templateLocation) {
        final var cl = ExtractionRunner.class.getClassLoader();
        final var resource = cl.getResourceAsStream(templateLocation);
        assert resource != null;

        return new BufferedReader(new InputStreamReader(resource)).lines().collect(Collectors.joining("\n"));
    }
}
