package com.redhat.documentation.asciidoc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.ast.Section;
import org.asciidoctor.jruby.AsciiDocDirectoryWalker;

public class ExtractionRunner {
    private Configuration config;

    public ExtractionRunner(Configuration conf) {
        this.config = conf;
    }

    public void run() {
        OptionsBuilder optionsBuilder = OptionsBuilder.options();
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        SectionTreeProcessor processor = new SectionTreeProcessor();

        // We need access to the line numbers and source
        optionsBuilder.sourcemap(true);

        // Have to register our instance of the extension so we can pull information from it
        asciidoctor.javaExtensionRegistry().treeprocessor(processor);

        for (File file : new AsciiDocDirectoryWalker(this.config.getSourceDirectory().getAbsolutePath())) {
            var doc = asciidoctor.loadFile(file, optionsBuilder.asMap());
            writeModules(processor);
            writeAssemblies(processor);
        }
    }

    private void writeAssemblies(SectionTreeProcessor processor) {
        // Setup templates for modules
        try {
            String templateStart = getTemplateContents("templates/start.adoc");
            String templateEnd = getTemplateContents("templates/end.adoc");

            for (Assembly a : processor.getAssemblies()) {
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
            }
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();

        }
    }

    private void writeModules(SectionTreeProcessor processor) {
        // Create the modules directory and write the files
        try {
            // Create the output directories
            Path modulesDir = Files.createDirectories(Paths.get(this.config.getOutputDirectory().getAbsolutePath(),
                                                        "modules"));

            for (ExtractedModule module : processor.getModules()) {
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
