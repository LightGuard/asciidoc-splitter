package com.redhat.documentation.asciidoc.extraction;

import com.redhat.documentation.asciidoc.Util;
import com.redhat.documentation.asciidoc.cli.ExtractionRunner;
import com.redhat.documentation.asciidoc.cli.Issue;
import com.redhat.documentation.asciidoc.extraction.model.Task;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.ast.Document;
import org.asciidoctor.jruby.AsciiDocDirectoryWalker;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Extractor {
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

        for (File file : new AsciiDocDirectoryWalker(this.task.getLocation().getDirectoryPath().toString())) {
            var doc = asciidoctor.loadFile(file, optionsBuilder.asMap());
            var lines = preprocessor.getLines();

            findSections(doc, lines);

            writeModules(this.task.getPushableLocation().getDirectoryPath());
            writeAssemblies(this.task.getPushableLocation().getDirectoryPath());
        }

        // Move all the extra assets
        moveNonadoc(this.task.getLocation().getDirectoryPath(), this.task.getPushableLocation().getDirectoryPath());
        this.task.getPushableLocation().push();

        long errors = this.issues.stream().filter(Issue::isError).count();

        System.out.println("Found " + this.issues.size() + " issues. " + errors + " Errors.");
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

        this.assemblies.forEach(a -> {
            try {
                // Create any directories that need to be created
                Path assembliesDir = Files
                        .createDirectories(outputDirectory.resolve("assemblies"));
                var outputFile = Paths.get(assembliesDir.toString(), a.getFilename());
                try (Writer output = new FileWriter(outputFile.toFile())) {
                    output.append(templateStart).append("\n").append(a.getSource()).append("\n").append(templateEnd);
                }
            } catch (IOException e) {
                // TODO: We blew-up in an unexpected way, handle this
                throw new RuntimeException(e);
            }
        });
    }

    private void writeModules(Path  targetDirectory) {
        // Create the modules directory and write the files
        try {
            // Create the output directories
            Path modulesDir = Files.createDirectories(targetDirectory.resolve("modules"));
            Set<Path> visitedPaths = new HashSet<>();

            for (ExtractedModule module : this.modules) {
                // Create output file
                Path moduleOutputFile = Paths.get(modulesDir.toString(), module.getFileName());

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

    private String getTemplateContents(String templateLocation) {
        final var cl = ExtractionRunner.class.getClassLoader();
        final var resource = cl.getResourceAsStream(templateLocation);
        assert resource != null;

        return new BufferedReader(new InputStreamReader(resource)).lines().collect(Collectors.joining("\n"));
    }
}

