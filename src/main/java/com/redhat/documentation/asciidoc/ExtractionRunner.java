package com.redhat.documentation.asciidoc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;
import org.asciidoctor.jruby.AsciiDocDirectoryWalker;

public class ExtractionRunner {
    private Configuration config;
    private List<Assembly> assemblies;
    private Set<ExtractedModule> modules;

    public ExtractionRunner(Configuration conf) {
        this.config = conf;

        this.assemblies = new ArrayList<>();
        this.modules = new HashSet<>();
    }

    public void run() {
        OptionsBuilder optionsBuilder = OptionsBuilder.options();
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();

        // We need access to the line numbers and source
        optionsBuilder.sourcemap(true);

        for (File file : new AsciiDocDirectoryWalker(this.config.getSourceDirectory().getAbsolutePath())) {
            var doc = asciidoctor.loadFile(file, optionsBuilder.asMap());

            findSections(doc);

            writeModules();
            writeAssemblies();
        }
    }

    private void findSections(Document doc) {
        // TODO: What should we do with preamble?
        // TODO: This should probably be configurable
        doc.getBlocks().forEach(node -> {
            if (node instanceof Section && node.getLevel() == 1) {
                var assembly = new Assembly((Section) node);
                this.assemblies.add(assembly);
                this.modules.addAll(assembly.getModules());
            }
        });
        // We should have all the assemblies, and all of their modules now
    }

    private void writeAssemblies() {
        // Setup templates for modules
        try {
            String templateStart = getTemplateContents("templates/start.adoc");
            String templateEnd = getTemplateContents("templates/end.adoc");

            this.assemblies.forEach(a -> {
                var outputFile = Paths.get(this.config.getOutputDirectory().getAbsolutePath(), a.getFilename());
                try (Writer output = new FileWriter(outputFile.toFile())) {
                    output.append(templateStart)
                            .append("\n\n")
                            .append(a.getSource())
                            .append("\n\n")
                            .append(templateEnd);
                } catch (IOException e) {
                    // TODO: better catch when we can't open or write
                    throw new RuntimeException(e);
                }
            });
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();

        }
    }

    private void writeModules() {
        // Create the modules directory and write the files
        try {
            // Create the output directories
            Path modulesDir = Files.createDirectories(Paths.get(this.config.getOutputDirectory().getAbsolutePath(),
                                                        "modules"));

            for (ExtractedModule module : this.modules) {
                // Create output file
                Path moduleOutputFile = Files.createFile(Paths.get(modulesDir.toString(), module.getFileName()));

                // Output the module
                try (Writer output = new FileWriter(moduleOutputFile.toFile())) {
                    output.append("\n\n")
                            // Adding the id of the module
                            .append("[id=\"").append(module.getId()).append("_{context}\"]\n")
                            // Adding the section title
                            .append("= ").append(module.getSection().getTitle()).append("\n")
                            // Adding the content of the module
                            .append(String.join("\n", module.getSources()))
                            .append("\n\n");
                }
            }

        } catch (IOException e) {
            // TODO: We blew-up handle this
            throw new RuntimeException(e);
        }
    }

    private String getTemplateContents(String templateLocation) throws URISyntaxException, IOException {
        final var cl = ExtractionRunner.class.getClassLoader();
        final var resource = cl.getResource(templateLocation);

        return new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(resource).toURI())));
    }
}
