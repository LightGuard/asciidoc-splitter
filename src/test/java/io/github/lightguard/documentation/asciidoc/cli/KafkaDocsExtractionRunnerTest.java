package io.github.lightguard.documentation.asciidoc.cli;

import org.assertj.core.data.Index;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Files;
import java.nio.file.Path;

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

    @ParameterizedTest(name = "pantheonV2: {0}")
    @ValueSource(booleans = {true, false})
    public void issue88NoteTest(boolean pantheonV2) throws Exception {
        executeRunner("docs/issue-88", pantheonV2);

        var pantheonV2LineOffset = (pantheonV2) ? 1 : 0;

        // assembly check
        var assembliesDir = this.outputDirPath.resolve("assemblies");
        var assembly = assembliesDir.resolve("assembly-note-test.adoc");
        assertThat(Files.readAllLines(assembly)).contains("[WARNING]", Index.atIndex(5 + pantheonV2LineOffset));
        assertThat(Files.readAllLines(assembly)).contains("====", Index.atIndex(6 + pantheonV2LineOffset));
        assertThat(Files.readAllLines(assembly)).contains("====", Index.atIndex(8 + pantheonV2LineOffset));

        // module check
        var modulesDir = this.outputDirPath.resolve("modules").resolve("issue-88");
        assertThat(modulesDir).exists();

        final Path module = modulesDir.resolve("proc-first-section.adoc");
        assertThat(module).exists();
        assertThat(Files.lines(module)).contains("[NOTE]", Index.atIndex(6 + pantheonV2LineOffset));
        assertThat(Files.lines(module)).contains("====", Index.atIndex(7 + pantheonV2LineOffset));
        assertThat(Files.lines(module)).contains("====", Index.atIndex(9 + pantheonV2LineOffset));
    }

    @ParameterizedTest(name = "pantheonV2: {0}")
    @ValueSource(booleans = {true, false})
    public void issue94ExtraBlankLineNecessary() throws Exception {
        executeRunner("docs/issue-94", false);

        var assemblyDoc = assembliesDir.resolve("assembly-getting-started-rhoas-cli-kafka.adoc");
        assertThat(assemblyDoc).exists();
        assertThat(Files.lines(assemblyDoc)).contains("== Prerequisites");
    }

    @ParameterizedTest(name = "pantheonV2: {0}")
    @ValueSource(booleans = {true, false})
    public void issue94endifGone() throws Exception {
        executeRunner("docs/issue-94-kafka", false);

        var assemblyDoc = assembliesDir.resolve("assembly-kafka-bin-scripts.adoc");
        assertThat(assemblyDoc).exists();
        assertThat(Files.readAllLines(assemblyDoc).get(55)).doesNotContain("endif::[]");
        assertThat(Files.lines(assemblyDoc)).contains("[#conclusion]");
    }
}
