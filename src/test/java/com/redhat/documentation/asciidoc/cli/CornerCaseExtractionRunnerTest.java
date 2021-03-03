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
        var modulesDir = new File(this.outputDirectory, "modules").toPath().resolve("issue-82").toFile();
        assertThat(modulesDir.exists()).isTrue();
        assertThat(modulesDir.listFiles()).hasSize(29);
        var moduleContent = Files.lines(modulesDir.toPath().resolve("con-kogito-operator-architecture.adoc"));

        assertThat(moduleContent.anyMatch(s -> s.startsWith("==="))).isFalse();
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
}
