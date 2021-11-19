package io.github.lightguard.documentation.asciidoc.cli;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class IgnoreFoldersTest extends ExtractionRunnerBase {
    @Test
    public void multipleIgnoreOptions() throws Exception {
        var sourceDir = new File("src/test/resources/docs/issue-93");
        var options = new String[]{"-s", sourceDir.getAbsolutePath(),
                "-o", this.outputDirectory.getAbsolutePath(),
                "-a", "KOGITO-ENT=true",
                "-i", "ignoreDir2,ignoreDir-1",
                "-i", "chap-ignore-file.adoc"
        };

        var command = CommandLine.populateCommand(new ExtractionRunner(), options);
        assertThat(command.ignoreFiles).contains(
                new File("ignoreDir2"),
                new File("ignoreDir-1"),
                new File("chap-ignore-file.adoc"));
    }

    @Test
    public void issue93IgnoreFolders() throws Exception {
        var sourceDir = new File("src/test/resources/docs/issue-93");
        var options = new String[]{"-s", sourceDir.getAbsolutePath(),
                "-o", this.outputDirectory.getAbsolutePath(),
                "-a", "KOGITO-ENT=true",
                "-i", "ignoreDir2,ignoreDir-1",
                "-i", "chap-ignore-file.adoc"
        };

        var exitCode = new CommandLine(new ExtractionRunner()).execute(options);
        assertThat(exitCode).isEqualTo(0);

        var assembly = outputDirectory.toPath().resolve("assemblies")
                .resolve("assembly-section-test.adoc");

        assertThat(assembly).exists();
        var modules = outputDirectory.toPath().resolve("modules");
        assertThat(modules.resolve("issue-93").resolve("proc-first-section.adoc")).exists();
        assertThat(modules.resolve("issue-93").resolve("proc-some-other.adoc")).doesNotExist();
    }
}
