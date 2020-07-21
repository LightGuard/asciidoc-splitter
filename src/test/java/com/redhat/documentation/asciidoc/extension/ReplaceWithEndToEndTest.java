package com.redhat.documentation.asciidoc.extension;

import java.io.File;
import java.nio.file.Files;

import com.redhat.documentation.asciidoc.cli.ExtractionRunner;
import com.redhat.documentation.asciidoc.cli.ExtractionRunnerBase;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import static org.assertj.core.api.Assertions.assertThat;

public class ReplaceWithEndToEndTest extends ExtractionRunnerBase {

    @Test
    public void endToEndReplaceWithTest() throws Exception {
        var sourceDirectory = new File("src/test/resources/docs/processor-test/end-to-end");
        var options = new String[]{"-s", sourceDirectory.getAbsolutePath(),
                "-o", outputDirectory.getAbsolutePath(),
                "-a", "KOGITO-ENT=true"
        };

        var exitCode = new CommandLine(new ExtractionRunner()).execute(options);
        assertThat(exitCode).isEqualTo(0);

        var chap = outputDirectory.toPath().resolve("assemblies")
                .resolve("assembly-kogito-configuring.adoc");

        var moduleWithReplacement = outputDirectory.toPath().resolve("modules/end-to-end/con-kogito-supporting-services-and-configuration.adoc");

        assertThat(Files.readString(moduleWithReplacement)).doesNotContain("link:{asciidoc-dir}/creating-running/chap-kogito-creating-running.adoc[My Test].");
        assertThat(Files.readString(moduleWithReplacement)).doesNotContain("[replace-with=\"chap-kogito-creating-running.adoc\" replace-with-id=\"ref-kogito-app-examples\"]");
        assertThat(Files.readString(moduleWithReplacement)).contains("include::modules/end-to-end/ref-kogito-app-examples.adoc[leveloffset=+1]");
    }
}
