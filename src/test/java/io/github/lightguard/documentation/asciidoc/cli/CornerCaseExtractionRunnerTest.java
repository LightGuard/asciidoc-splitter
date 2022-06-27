package io.github.lightguard.documentation.asciidoc.cli;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

public class CornerCaseExtractionRunnerTest extends ExtractionRunnerBase {
    @Test
    public void subheadingIssue82Test() throws Exception {
        final var sourceDirectory = new File(ExtractionRunner.class.getClassLoader().getResource("docs/issue-82").toURI());
        var options = new String[]{"-s", sourceDirectory.getAbsolutePath(), "-o", this.outputDirectory.getAbsolutePath()};

        new CommandLine(new ExtractionRunner()).execute(options);

        // Modules
        var modulesDir = new File(this.outputDirectory, "modules").toPath().resolve("issue-82").toFile();
        assertThat(modulesDir.exists()).isTrue();
        assertThat(modulesDir.listFiles()).hasSize(29);
        var moduleContent = Files.readAllLines(modulesDir.toPath().resolve("con-kogito-operator-architecture.adoc"));

        assertThat(moduleContent).contains("== {PRODUCT} Operator dependencies on third-party operators");
    }

    @Test
    public void nestingIssue78Test() throws Exception {
        final var sourceDirectory = new File(ExtractionRunner.class.getClassLoader().getResource("docs/issue-78").toURI());
        var options = new String[]{"-s", sourceDirectory.getAbsolutePath(), "-o", this.outputDirectory.getAbsolutePath()};

        new CommandLine(new ExtractionRunner()).execute(options);

        // Modules
        var modulesDir = new File(this.outputDirectory, "modules");
        assertThat(modulesDir).exists();
        var topicDir = new File(modulesDir, "sub-dir");
        assertThat(topicDir).exists();
        assertThat(modulesDir.toPath().resolve("sub-sub-dir")).exists();
        assertThat(new File(topicDir, "issue-78")).doesNotExist();
    }

    @Test
    public void onlyChapFilesIssue79() throws Exception {
        final var sourceDirectory = new File(ExtractionRunner.class.getClassLoader().getResource("docs/issue-79").toURI());
        var options = new String[]{"-s", sourceDirectory.getAbsolutePath(), "-o", this.outputDirectory.getAbsolutePath()};

        new CommandLine(new ExtractionRunner()).execute(options);

        var modulesDir = new File(this.outputDirectory, "modules");
        var assembliesDir = new File(this.outputDirectory, "assemblies");
        assertThat(assembliesDir).exists();
        assertThat(modulesDir).exists();

        assertThat(assembliesDir.toPath().resolve("ref-dmn-feel-builtin-functions.adoc")).doesNotExist();
        assertThat(assembliesDir.toPath().resolve("assembly-ref-dmn-feel-builtin-functions.adoc")).doesNotExist();
        assertThat(modulesDir.toPath().resolve("ref-dmn-feel-builtin-functions.adoc")).exists();
    }

    @Test
    public void conditionalIssue80Test() throws Exception {
        final var sourceDirectory = new File(ExtractionRunner.class.getClassLoader().getResource("docs/issue-80").toURI());
        var options = new String[]{"-s", sourceDirectory.getAbsolutePath(), "-o", this.outputDirectory.getAbsolutePath()};

        new CommandLine(new ExtractionRunner()).execute(options);

        var assemblies = new File(this.outputDirectory, "assemblies");
        assertThat(assemblies.exists()).isTrue();
        assertThat(assemblies.listFiles(pathname -> pathname.getName().contains(".adoc"))).hasSize(1);

        var assemblyFile = assemblies.toPath().resolve("assembly-kogito-using-dmn-models.adoc");
        assertThat(Files.lines(assemblyFile)).contains("ifdef::KOGITO-COMM[]");
        assertThat(Files.lines(assemblyFile)).contains("include::modules/dmn/ref-dmn-feel-enhancements.adoc[leveloffset=+2]");
        assertThat(Files.lines(assemblyFile)).contains("include::modules/dmn/ref-dmn-model-enhancements.adoc[leveloffset=+2]");
        assertThat(Files.lines(assemblyFile)).contains("endif::[]");

        assertThat(Files.lines(assemblyFile)).contains("ifdef::KOGITO-ENT[]");
    }

    @Test
    public void missingModulesIssue80AndIssue81Test() throws Exception {
        final var sourceDirectory = new File(ExtractionRunner.class.getClassLoader().getResource("docs/missing-modules").toURI());
        var options = new String[]{"-s", sourceDirectory.getAbsolutePath(), "-o", this.outputDirectory.getAbsolutePath()};

        new CommandLine(new ExtractionRunner()).execute(options);

        var assemblies = new File(this.outputDirectory, "assemblies");
        var modulesDir = new File(this.outputDirectory, "modules");
        assertThat(assemblies.exists()).isTrue();
        assertThat(assemblies.listFiles(pathname -> pathname.getName().contains(".adoc"))).hasSize(1);

        assertThat(modulesDir.exists()).isTrue();
        assertThat(modulesDir.toPath().resolve("configuration").resolve("con-grafana-dashboards-metrics-monitoring.adoc")).exists();
    }

    @Test
    public void nestedIfdefTest() throws Exception {
        final var sourceDirectory = new File(ExtractionRunner.class.getClassLoader().getResource("docs/nested-ifdef").toURI());
        var options = new String[]{"-s", sourceDirectory.getAbsolutePath(), "-o", this.outputDirectory.getAbsolutePath()};

        new CommandLine(new ExtractionRunner()).execute(options);

        var assemblies = new File(this.outputDirectory, "assemblies");
        var modulesDir = new File(this.outputDirectory, "modules");

        assertThat(assemblies.exists()).isTrue();
        assertThat(assemblies.toPath().resolve("assembly-kogito-creating-running.adoc")).exists();

        var assemblyLines = Files.readAllLines(assemblies.toPath().resolve("assembly-kogito-creating-running.adoc"));

        // single line preprocessor
        assertThat(assemblyLines.get(3)).startsWith("ifdef::");

        assertThat(assemblyLines.get(14)).startsWith("ifdef::KOGITO-ENT[]");
        assertThat(assemblyLines.get(23)).startsWith("endif::[]");

        assertThat(assemblyLines.get(50)).doesNotContain("endif:[]");

        assertThat(assemblyLines.get(52)).startsWith("ifdef::KOGITO-ENT[]");
        assertThat(assemblyLines.get(57)).startsWith("endif::");

        var moduleLines = Files.readAllLines(modulesDir.toPath().resolve("nested-ifdef").resolve("con-kogito-automation.adoc"));
        assertThat(moduleLines.get(36)).startsWith("ifdef::KOGITO-COMM[]");
        assertThat(moduleLines.get(40)).startsWith("endif::[]");

        moduleLines = Files.readAllLines(modulesDir.toPath().resolve("nested-ifdef").resolve("ref-kogito-glossary.adoc"));
        assertThat(moduleLines).doesNotContain("Additional Resources");

        moduleLines = Files.readAllLines(modulesDir.toPath().resolve("nested-ifdef").resolve("con-kogito-quarkus-springboot.adoc"));
        assertThat(moduleLines).hasSize(16);
        assertThat(moduleLines).doesNotContain("endif::[]");

        moduleLines = Files.readAllLines(modulesDir.toPath().resolve("nested-ifdef").resolve("proc-kogito-running-app.adoc"));
        assertThat(moduleLines).haveExactly(2, new Condition<>(s -> "endif::[]".equals(s), "endif"));
    }

    @Test
    @Disabled("Not sure what to do here")
    public void includeTagSections() throws Exception {
        final var sourceDirectory = new File(ExtractionRunner.class.getClassLoader().getResource("docs/nested-ifdef").toURI());
        var options = new String[]{"-s", sourceDirectory.getAbsolutePath(), "-o", this.outputDirectory.getAbsolutePath()};

        new CommandLine(new ExtractionRunner()).execute(options);

        var assemblies = this.outputDirectory.toPath().resolve("assemblies");
        var modulesDir = this.outputDirectory.toPath().resolve("modules");

        var assemblyLines = Files.readAllLines(assemblies.resolve("assembly-kogito-creating-running.adoc"));
        assertThat(assemblyLines).doesNotContain("// tag::con-kogito-automation[]\n");
        assertThat(assemblyLines).doesNotContain("// end::con-kogito-automation[]\n");


        var moduleLines = Files.readAllLines(modulesDir.resolve("nested-ifdef").resolve("con-kogito-automation.adoc"));
        assertThat(moduleLines).contains("// tag::con-kogito-automation[]\n");
        assertThat(moduleLines).contains("// end::con-kogito-automation[]\n");
    }

    @Test
    public void additionalResources81Test() throws Exception {
        final var sourceDirectory = new File(ExtractionRunner.class.getClassLoader().getResource("docs/issue-80").toURI());
        var options = new String[]{"-s", sourceDirectory.getAbsolutePath(), "-o", this.outputDirectory.getAbsolutePath()};

        new CommandLine(new ExtractionRunner()).execute(options);

        var assemblies = new File(this.outputDirectory, "assemblies");
        assertThat(assemblies.exists()).isTrue();
        assertThat(assemblies.listFiles(pathname -> pathname.getName().contains(".adoc"))).hasSize(1);

        var assemblyFile = assemblies.toPath().resolve("assembly-kogito-using-dmn-models.adoc");
        assertThat(Files.lines(assemblyFile)).contains("[role=\"_additional-resources\"]");
        assertThat(Files.lines(assemblyFile)).contains("* {URL_CREATING_RUNNING}[_{CREATING_RUNNING}_]");
        assertThat(Files.lines(assemblyFile)).contains("* {URL_DEPLOYING_ON_OPENSHIFT}[_{DEPLOYING_ON_OPENSHIFT}_]");
        assertThat(Files.lines(assemblyFile)).contains("* {URL_PROCESS_SERVICES}[_{PROCESS_SERVICES}_]");
        assertThat(Files.lines(assemblyFile)).contains("* {URL_CONFIGURING_KOGITO}[_{CONFIGURING_KOGITO}_]");
    }

    @Test
    public void assemblyInsteadOfChap95Test() throws Exception {
        this.executeRunner("docs/issue-95", false);

        assertThat(assembliesDir.toFile().exists()).isTrue();
        assertThat(assembliesDir.toFile().listFiles(pathname -> pathname.getName().contains(".adoc"))).hasSize(1);

        var assemblyFile = assembliesDir.resolve("assembly-kogito-using-dmn-models.adoc");
        assertThat(Files.lines(assemblyFile)).contains("[id='assembly-kogito-using-dmn-models']");

        modulesDir = modulesDir.resolve("dmn");
        assertThat(modulesDir.toFile().exists()).isTrue();
        assertThat(modulesDir.toFile().listFiles(pathname -> pathname.getName().contains(".adoc"))).hasSizeGreaterThanOrEqualTo(1);
    }
}
