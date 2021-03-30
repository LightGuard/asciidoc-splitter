package com.redhat.documentation.asciidoc.cli;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

public class KafkaDocsExtractionRunnerTest extends ExtractionRunnerBase {
    @ParameterizedTest(name = "pantheonV2: {0}")
    @ValueSource(booleans = {true, false})
    public void ifdefNotCommentedTest(boolean pantheonV2) throws Exception {
        executeRunner("docs/kafka-ifdef", pantheonV2);

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

    @ParameterizedTest(name = "pantheonV2: {0}")
    @ValueSource(booleans = {true, false})
    public void sectionTest(boolean pantheonV2) throws Exception {
        executeRunner("docs/issue-87", pantheonV2);

        // module check
        var modulesDir = this.outputDirPath.resolve("modules").resolve("issue-87");
        assertThat(modulesDir).exists();

        final Path module = modulesDir.resolve("proc-first-section.adoc");
        assertThat(module).exists();
        assertThat(Files.lines(module)).contains("== Third Section");
        assertThat(Files.lines(module)).contains("=== Fourth Section");
    }
}
