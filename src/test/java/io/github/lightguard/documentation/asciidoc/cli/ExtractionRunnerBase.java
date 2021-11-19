package io.github.lightguard.documentation.asciidoc.cli;

import io.github.lightguard.documentation.asciidoc.extraction.DeletionFileVisitor;
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

import static org.assertj.core.api.Assertions.assertThat;

public class ExtractionRunnerBase {
    public static final String KOGITO_ASCIIDOC_FOLDER = "src/test/resources/docs/examples/kogito/input/doc-content/src/main/asciidoc";
    protected File outputDirectory;
    protected Path outputDirPath;
    protected Path assembliesDir;
    protected Path modulesDir;

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

    int executeRunner(String sourceDir, boolean pantheonV2) throws URISyntaxException {
        final var sourceDirectory = new File(ExtractionRunner.class.getClassLoader().getResource(sourceDir).toURI());
        final List<String> options = new ArrayList<>(List.of("-s", sourceDirectory.getAbsolutePath(),
                "-o", this.outputDirectory.getAbsolutePath()));

        if (pantheonV2)
            options.add("--pantheonV2");

        var exitCode = new CommandLine(new ExtractionRunner()).execute(options.toArray(new String[]{}));

        assembliesDir = this.outputDirPath.resolve("assemblies");
        modulesDir = this.outputDirPath.resolve("modules");

        assertThat(assembliesDir).exists();
        assertThat(modulesDir).exists();

        return exitCode;
    }
}
