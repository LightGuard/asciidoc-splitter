package io.github.lightguard.documentation.asciidoc.extension;

import java.io.File;
import java.nio.file.Files;

import io.github.lightguard.documentation.asciidoc.cli.ExtractionRunner;
import io.github.lightguard.documentation.asciidoc.cli.ExtractionRunnerBase;
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
        assertThat(Files.readString(moduleWithReplacement)).doesNotContain("[replace-with=\"../creating-running/ref-kogito-app-examples.adoc\" replace-with-param=\"leveloffset=+1\"]");
        assertThat(Files.readString(moduleWithReplacement)).contains("include::../../modules/end-to-end/ref-kogito-app-examples.adoc[leveloffset=+1]");
    }
//
//    @Test
//    public void openBlockTest() throws Exception {
//        var sourceDirectory = new File("src/test/resources/docs/processor-test/openblock");
//        var options = new String[]{"-s", sourceDirectory.getAbsolutePath(),
//                "-o", outputDirectory.getAbsolutePath(),
//                "-a", "KOGITO-ENT=true"
//        };
//
//        var exitCode = new CommandLine(new ExtractionRunner()).execute(options);
//        assertThat(exitCode).isEqualTo(0);
//
//        var chap = outputDirectory.toPath().resolve("assemblies")
//                .resolve("assembly-replace-with-openblock.adoc");
//
//        var moduleWithReplacement = outputDirectory.toPath().resolve("modules/openblock/con-new-section.adoc");
//
//        assertThat(Files.readString(moduleWithReplacement)).doesNotContain("[replace-with=\"creating-services/con-new-service.adoc\" replace-with-params=\"leveloffset=+1\"]");
//        assertThat(Files.readString(moduleWithReplacement)).contains("include::../../modules/openblock/con-new-service.adoc[leveloffset=+1]");
//    }
}
