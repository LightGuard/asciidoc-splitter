package com.redhat.documentation.asciidoc.cli;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import static org.assertj.core.api.Assertions.assertThat;

public class KafkaDocsExtractionRunnerTest extends ExtractionRunnerBase {
    @Test
    public void ifdefNotCommentedTest() throws Exception {
        final var sourceDirectory = new File(ExtractionRunner.class.getClassLoader().getResource("docs/kafka-ifdef").toURI());
        var options = new String[]{"-s", sourceDirectory.getAbsolutePath(),
                "-o", this.outputDirectory.getAbsolutePath(),
                "--pantheonV2"
        };

        new CommandLine(new ExtractionRunner()).execute(options);

        // assembly
        var assemblyDir = this.outputDirPath.resolve("assemblies");
        assertThat(assemblyDir).exists();

        final Path gettingStartedAdoc = assemblyDir.resolve("assembly-getting-started.adoc");
        assertThat(gettingStartedAdoc).exists();
        assertThat(Files.lines(gettingStartedAdoc)).doesNotContain("// -- splitter comment -- ifndef::community[]");

        // Modules
        var modulesDir = this.outputDirPath.resolve("modules").resolve("kafka-ifdef");
        assertThat(modulesDir).exists();
    }
}
