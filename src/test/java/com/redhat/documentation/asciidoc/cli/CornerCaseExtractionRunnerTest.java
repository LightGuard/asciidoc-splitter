package com.redhat.documentation.asciidoc.cli;

import java.io.File;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import static org.assertj.core.api.Assertions.assertThat;

public class CornerCaseExtractionRunnerTest extends ExtractionRunnerBase {
    @Test
    public void subheadingIssue82Test() throws Exception {
        final var sourceDirectory = new File(ExtractionRunner.class.getClassLoader().getResource("docs/issue-82").toURI());
        var options = new String[]{"-s", sourceDirectory.getAbsolutePath(), "-o", this.outputDirectory.getAbsolutePath()};

        new CommandLine(new ExtractionRunner()).execute(options);

        // Modules
        var modulesDir = new File(this.outputDirectory, "modules");
        assertThat(modulesDir.exists()).isTrue();
        var topicDir = new File(modulesDir, "issue-82");
        assertThat(modulesDir.listFiles()).hasSize(1);
        assertThat(topicDir.exists()).isTrue();
        assertThat(topicDir.listFiles()).hasSize(29);
        var moduleContent = Files.lines(topicDir.toPath().resolve("con-kogito-operator-architecture.adoc"));

        assertThat(moduleContent.anyMatch(s -> s.startsWith("==="))).isFalse();
    }
}
