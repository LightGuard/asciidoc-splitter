package com.redhat.documentation.asciidoc.cli;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

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
        assertThat(modulesDir.listFiles()).hasSize(29);
        var moduleContent = Files.lines(modulesDir.toPath().resolve("con-kogito-operator-architecture.adoc"));

        assertThat(moduleContent.anyMatch(s -> s.startsWith("==="))).isFalse();
    }

    @Test
    public void nestingIssue79Test() throws Exception {
        final var sourceDirectory = new File(ExtractionRunner.class.getClassLoader().getResource("docs/issue-78").toURI());
        var options = new String[]{"-s", sourceDirectory.getAbsolutePath(), "-o", this.outputDirectory.getAbsolutePath()};

        new CommandLine(new ExtractionRunner()).execute(options);

        // Modules
        var modulesDir = new File(this.outputDirectory, "modules");
        assertThat(modulesDir).exists();
        var topicDir = new File(modulesDir, "sub-dir");
        assertThat(topicDir).exists();
        assertThat(Path.of(topicDir.toPath().toString(), "sub-sub-dir")).exists();
        assertThat(new File(topicDir, "issue-78")).doesNotExist();
    }
}
