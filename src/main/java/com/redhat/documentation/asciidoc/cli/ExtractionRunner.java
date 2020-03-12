package com.redhat.documentation.asciidoc.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.redhat.documentation.asciidoc.Configuration;
import com.redhat.documentation.asciidoc.extraction.Assembly;
import com.redhat.documentation.asciidoc.extraction.ExtractedModule;
import com.redhat.documentation.asciidoc.extraction.SourceExtractor;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.jruby.AsciiDocDirectoryWalker;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "extract", mixinStandardHelpOptions = true, version = "1.0",
         description = "Create a modular documentation layout from a directory of asciidoc files.")
public class ExtractionRunner implements Callable<Integer> {
    private List<Assembly> assemblies;
    private Set<ExtractedModule> modules;

    @Option(names = {"-s", "--sourceDir"}, description = "Directory containing the input asciidoc files.", required = true)
    File inputDir;

    @Option(names = {"-o", "--outputDir"}, description = "Directory to place generated modules and assemblies.", required = true)
    File outputDir;

    public ExtractionRunner() {
        this.assemblies = new ArrayList<>();
        this.modules = new HashSet<>();
    }

    public static void main(String... args) {
        var exitCode = new CommandLine(new ExtractionRunner()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        var config = new Configuration(this.inputDir, this.outputDir);

        OptionsBuilder optionsBuilder = OptionsBuilder.options();
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();

        // We need access to the line numbers and source
        optionsBuilder.sourcemap(true);

        for (File file : new AsciiDocDirectoryWalker(config.getSourceDirectory().getAbsolutePath())) {
            var doc = asciidoctor.loadFile(file, optionsBuilder.asMap());

            findSections(doc);

            writeModules(config);
            writeAssemblies(config);
        }

        return 0;
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

    private void writeAssemblies(Configuration config) {
        // Setup templates for modules
        String templateStart = getTemplateContents("templates/start.adoc");
        String templateEnd = getTemplateContents("templates/end.adoc");

        this.assemblies.forEach(a -> {
            var outputFile = Paths.get(config.getOutputDirectory().getAbsolutePath(), a.getFilename());
            try (Writer output = new FileWriter(outputFile.toFile())) {
                output.append(templateStart)
                        .append("\n")
                        .append(a.getSource())
                        .append("\n")
                        .append(templateEnd);
            } catch (IOException e) {
                // TODO: better catch when we can't open or write
                throw new RuntimeException(e);
            }
        });
    }

    private void writeModules(Configuration config) {
        // Create the modules directory and write the files
        try {
            // Create the output directories
            Path modulesDir = Files.createDirectories(Paths.get(config.getOutputDirectory().getAbsolutePath(),
                                                        "modules"));

            Set<Path> accessedPaths = new HashSet<>();

            for (ExtractedModule module : this.modules) {
                // Create output file
                Path wantedPath = Paths.get(modulesDir.toString(), module.getFileName());

                Path moduleOutputFile = null;
                if(!wantedPath.toFile().exists()) {
                        System.out.println("Creating new file: " + wantedPath + " for module " + module.getId());
                        moduleOutputFile = Files.createFile(wantedPath);
                } else {
                    if(accessedPaths.contains(wantedPath)) {
                        System.out.println("File already existed: " + wantedPath + " for module " + module.getId());
                        moduleOutputFile = wantedPath;
                    } else {
                        //System.out.println("Overwriting file " + wantedPath);
                        moduleOutputFile = wantedPath;
                    }
                }

                accessedPaths.add(moduleOutputFile);

                // Output the module
                try (Writer output = new FileWriter(moduleOutputFile.toFile())) {
                    output
                            // Adding the id of the module
                            .append("[id=\"").append(module.getId()).append("_{context}\"]\n")
                            // Adding the section title
                            .append("= ").append(module.getSection().getTitle()).append("\n\n");

                    for (Iterator<String> iterator = module.getSources().iterator(); iterator.hasNext(); ) {
                        String section = iterator.next();
                        output.append(section);

                        Pattern coPattern = Pattern.compile("<(!--)?(\\d+|\\.)(--)?>");
                        var hasCallout = coPattern.matcher(section).find();

                        // Use a single new line for source sections with callouts
                        // otherwise a blank line between sections is what is needed
                        if (iterator.hasNext()) { // If it is the last section, we don't need a newline
                            if (hasCallout)
                                output.append("\n");
                            else
                                output.append("\n\n");
                        }
                    }
                }
            }

        } catch (IOException e) {
            // TODO: We blew-up handle this
            throw new RuntimeException(e);
        }
    }

    private String getTemplateContents(String templateLocation) {
        final var cl = ExtractionRunner.class.getClassLoader();
        final var resource = cl.getResourceAsStream(templateLocation);
        assert resource != null;

        return new BufferedReader(new InputStreamReader(resource)).lines().collect(Collectors.joining("\n"));
    }
}
