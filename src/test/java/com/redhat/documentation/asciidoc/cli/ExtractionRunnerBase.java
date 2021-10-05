package com.redhat.documentation.asciidoc.cli;

import com.redhat.documentation.asciidoc.extraction.DeletionFileVisitor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ExtractionRunnerBase {
    public static final String KOGITO_ASCIIDOC_FOLDER = "src/test/resources/docs/examples/kogito/input/doc-content/src/main/asciidoc";
    protected File outputDirectory;
    protected Path outputDirPath;

    @BeforeAll
    static void allSetUp() {
        System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");
    }

    @BeforeEach
    void setUp() throws Exception {
        this.outputDirectory = new File("target/output-docs");
        this.outputDirPath = this.outputDirectory.toPath();

        if (this.outputDirectory.exists()) {
            tearDown(); // clean-up from a previous botched run
        }
        Files.createDirectory(this.outputDirectory.toPath());
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.walkFileTree(outputDirectory.toPath(), new DeletionFileVisitor());
    }

    void executeRunner(String sourceDir, boolean pantheonV2) throws URISyntaxException {
        final var sourceDirectory = new File(ExtractionRunner.class.getClassLoader().getResource(sourceDir).toURI());
        final List<String> options = new ArrayList<>(List.of("-s", sourceDirectory.getAbsolutePath(),
                "-o", this.outputDirectory.getAbsolutePath()));

        if (pantheonV2)
            options.add("--pantheonV2");

        new CommandLine(new ExtractionRunner()).execute(options.toArray(new String[]{}));
    }
}
