package io.github.lightguard.documentation.asciidoc;

import java.io.File;

import io.github.lightguard.documentation.asciidoc.cli.ExtractionRunner;
import io.github.lightguard.documentation.asciidoc.cli.ExtractionRunnerBase;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import static org.assertj.core.api.Assertions.assertThat;

public class ProblemFerret extends ExtractionRunnerBase {
    @Test
    @Disabled
    public void testNullPointerFinder() throws Exception {
        final var sourceDirectory = new File(ExtractionRunner.class.getClassLoader().getResource("docs/glossary-test").toURI());
        var options = new String[]{"-s", sourceDirectory.getAbsolutePath(), "-o", this.outputDirectory.getAbsolutePath()};

        var exitCode = new CommandLine(new ExtractionRunner()).execute(options);

        assertThat(exitCode).isEqualTo(0);
    }
}
