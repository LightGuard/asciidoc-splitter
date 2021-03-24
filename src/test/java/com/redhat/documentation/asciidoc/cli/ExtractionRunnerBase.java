package com.redhat.documentation.asciidoc.cli;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.redhat.documentation.asciidoc.extraction.DeletionFileVisitor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public class ExtractionRunnerBase {
    public static final String KOGITO_ASCIIDOC_FOLDER = "./examples/kogito/input/doc-content/src/main/asciidoc";
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
}
