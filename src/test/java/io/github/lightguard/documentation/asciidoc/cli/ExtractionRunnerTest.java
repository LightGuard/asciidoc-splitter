package io.github.lightguard.documentation.asciidoc.cli;

import io.github.lightguard.documentation.asciidoc.extension.ReaderPreprocessor;
import io.github.lightguard.documentation.asciidoc.extraction.AsciidocFileFilter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ExtractionRunnerTest extends ExtractionRunnerBase {

    @Test
//    @Disabled("bad assumptions in source document")
    void testFileContentsSourceBlock() throws Exception {
        final var sourceDirectory = new File(ExtractionRunner.class.getClassLoader().getResource("docs/content-test").toURI());
        var options = new String[]{"-s", sourceDirectory.getAbsolutePath(), "-o", this.outputDirectory.getAbsolutePath()};

        new CommandLine(new ExtractionRunner()).execute(options);

        // Modules
        var modulesDir = new File(this.outputDirectory, "modules");
        assertThat(modulesDir.exists()).isTrue();
        var topicDir = new File(modulesDir, "content-test");
        assertThat(modulesDir.listFiles()).hasSize(1);
        assertThat(topicDir.exists()).isTrue();
        var topicDirFileNames = Objects.requireNonNull(topicDir.listFiles(new AsciidocFileFilter()));
        assertThat(Arrays.stream(topicDirFileNames).map(File::getName).collect(Collectors.toList()))
                .containsExactlyInAnyOrder("proc-module-one.adoc", "con-module-two.adoc");

        // Assemblies
        var assembliesDir = new File(this.outputDirectory, "assemblies");
        assertThat(assembliesDir.listFiles(new AsciidocFileFilter())).hasSize(1);
        final var assemblyFile = Objects.requireNonNull(assembliesDir.listFiles(new AsciidocFileFilter()))[0];
        assertThat(assemblyFile.getName()).isEqualTo("assembly-one.adoc");
    }

    @Test
    public void testDocTeamExample() throws Exception {
        final var sourceDirectory = new File("src/test/resources/docs/examples/sample/input");
        var options = new String[]{"-s", sourceDirectory.getAbsolutePath(), "-o", this.outputDirectory.getAbsolutePath()};

        new CommandLine(new ExtractionRunner()).execute(options);

        // Modules
        var modulesDir = new File(this.outputDirectory, "modules");
        assertThat(modulesDir.exists()).isTrue();
        assertThat(modulesDir.listFiles()).hasSize(1);
        assertThat(modulesDir.toPath().resolve("input").toFile().listFiles()).hasSize(6);

        // Assemblies
        var assembliesDir = new File(this.outputDirectory, "assemblies");
        assertThat(assembliesDir.listFiles(new AsciidocFileFilter())).hasSize(1);

        final Path assemblyFile = assembliesDir.toPath().resolve("assembly-monitoring.adoc");
        assertThat(assemblyFile).exists();
        assertThat(Files.readString(assemblyFile)).contains("= Monitoring {ProductName}");

        // Check for ifdef correctness
        var outputFile = modulesDir.toPath().resolve("input").resolve("proc-deploy-monitoring-infrastructure.adoc").toFile();
        assertThat(Files.readString(outputFile.toPath())).doesNotContain(ReaderPreprocessor.SPLITTER_COMMENT + "ifeval::[\"{cmdcli}\" == \"oc\"]");
        assertThat(Files.readString(outputFile.toPath())).doesNotContain(ReaderPreprocessor.SPLITTER_COMMENT + "endif::[]");
        assertThat(Files.readString(outputFile.toPath())).contains("ifeval::[\"{cmdcli}\" == \"oc\"]");
        assertThat(Files.readString(outputFile.toPath())).contains("endif::[]");
    }

    @Test
    public void testKogitoCreatingExample() throws Exception {
        final var sourceDirectory = new File(KOGITO_ASCIIDOC_FOLDER);
        var options = new String[]{"-s", sourceDirectory.getAbsolutePath(), "-o", this.outputDirectory.getAbsolutePath(), "-i", "index.adoc"};

        new CommandLine(new ExtractionRunner()).execute(options);

        // Modules
        var modulesDir = new File(this.outputDirectory, "modules");
        assertThat(modulesDir.exists()).isTrue();
        assertThat(modulesDir.listFiles()).hasSize(1);
        var topicDir = new File(modulesDir, "creating-running");
        assertThat(topicDir.exists()).isTrue();
        assertThat(topicDir.listFiles()).hasSize(10);

        // Assemblies
        var assembliesDir = new File(this.outputDirectory, "assemblies");
        assertThat(assembliesDir.listFiles(new AsciidocFileFilter())).hasSize(1);
    }

    @Test
    public void testArtifactsAndImages() throws Exception {
        final var sourceDirectory = new File("src/test/resources/docs/examples/kogito/input");

        var options = new String[]{"-s", sourceDirectory.getAbsolutePath(), "-o", this.outputDirectory.getAbsolutePath()};

        new CommandLine(new ExtractionRunner()).execute(options);

        var artifactsDir = new File(outputDirectory, "_artifacts");
        var imagesDir = new File(outputDirectory, "_images");

        assertThat(artifactsDir.exists()).isTrue();
        assertThat(imagesDir.exists()).isTrue();

        // symlink for _images in modules and imagesdir in modules and assembly files
        assertThat(outputDirectory.toPath().resolve("assemblies").resolve("_images")).isSymbolicLink();
        assertThat(outputDirectory.toPath().resolve("modules").resolve("_images")).isSymbolicLink();
    }

    @Test
    public void testSymlinkCreationUnderAssemblies() throws Exception {
        final var sourceDirectory = new File("src/test/resources/docs/examples/kogito/input");
        var options = new String[]{"-s", sourceDirectory.getAbsolutePath(), "-o", this.outputDirectory.getAbsolutePath()};

        new CommandLine(new ExtractionRunner()).execute(options);

        assertThat(Files.exists(Paths.get(this.outputDirectory.toString(), "assemblies", "modules"))).isTrue();
        assertThat(Files.isSymbolicLink(Paths.get(this.outputDirectory.toString(), "assemblies", "modules"))).isTrue();

        assertThat(Files.exists(Paths.get(this.outputDirectory.toString(), "assemblies", "_images"))).isTrue();
        assertThat(Files.isSymbolicLink(Paths.get(this.outputDirectory.toString(), "assemblies", "_images"))).isTrue();

        assertThat(Files.exists(Paths.get(this.outputDirectory.toString(), "assemblies", "_artifacts"))).isTrue();
        assertThat(Files.isSymbolicLink(Paths.get(this.outputDirectory.toString(), "assemblies", "_artifacts"))).isTrue();
    }

    @Test
    public void testTitleDirectoryContents() throws Exception {
        final var sourceDirectory = new File("src/test/resources/docs/examples/kogito/input");
        var options = new String[]{"-s", sourceDirectory.getAbsolutePath(), "-o", this.outputDirectory.getAbsolutePath()};

        new CommandLine(new ExtractionRunner()).execute(options);

        final File titleDirectory = new File(this.outputDirectory, "titles-enterprise");
        assertThat(titleDirectory.isDirectory()).isTrue();
        assertThat(titleDirectory.list()).containsOnly("kogito-configuring", "assemblies-test", "master-docinfo.xml", "index.adoc");

        // Check for assemblies and not doc-content
        var kogitoConfiguringDir = titleDirectory.toPath().resolve("kogito-configuring");
        assertThat(kogitoConfiguringDir.toFile().list()).containsOnly("master-docinfo.xml", "master.adoc", "assemblies");
        assertThat(kogitoConfiguringDir.resolve("assemblies")).isSymbolicLink();
        assertThat(Files.readSymbolicLink(kogitoConfiguringDir.resolve("assemblies"))).isEqualTo(Path.of("..", "..", "assemblies"));

        // Check for correct includes in master.adoc
        var masterDoc = kogitoConfiguringDir.resolve("master.adoc");
        assertThat(Files.readString(masterDoc)).contains("include::assemblies/assembly-kogito-configuring.adoc[]");
        assertThat(Files.readString(masterDoc)).contains("include::title.adoc[]");
        assertThat(Files.readString(masterDoc)).doesNotContain("include::modules/title.adoc[]");
    }

    @Test
    public void testAssemblyFalseOutput() throws Exception {
        final var sourceDirectory = new File("./src/test/resources/docs/no-assembly");
        var options = new String[]{"-s", sourceDirectory.getAbsolutePath(), "-o", this.outputDirectory.getAbsolutePath()};

        new CommandLine(new ExtractionRunner()).execute(options);

        final File assembliesDir = new File(this.outputDirectory, "assemblies");
        assertThat(assembliesDir.list()).containsOnly("modules");

        // Modules
        final File modulesDir = new File(this.outputDirectory, "modules");
        assertThat(modulesDir.list()).isNotEmpty();
    }

    // Test for Issue #60
    @Test
    public void testAdditionalResources() throws Exception {
        final var sourceDirectory = new File("src/test/resources/docs/examples/kogito/additional_resources");
        var options = new String[]{"-s", sourceDirectory.getAbsolutePath(),
                "-o", this.outputDirectory.getAbsolutePath(),
                "-a", "KOGITO-ENT=true"
        };

        new CommandLine(new ExtractionRunner()).execute(options);

        var chap = outputDirectory.toPath().resolve("assemblies")
                .resolve("assembly-kogito-developing-decision-services.adoc");

        assertThat(Files.readString(chap)).contains("== Additional resources");
    }

    @Test
    public void testIgnoreFiles() throws Exception {
        final var sourceDirectory = new File("src/test/resources/docs/examples/kogito/ignore_files/asciidoc");
        var options = new String[]{"-s", sourceDirectory.getAbsolutePath(),
                "-o", this.outputDirectory.getAbsolutePath(),
                "-i", "index.adoc"
        };

        new CommandLine(new ExtractionRunner()).execute(options);

        // Modules
        var modulesDir = new File(this.outputDirectory, "modules");
        assertThat(modulesDir.exists()).isTrue();
        assertThat(modulesDir.listFiles()).hasSize(1);
        var topicDir = new File(modulesDir, "creating-running");
        assertThat(topicDir.exists()).isTrue();
        assertThat(topicDir.listFiles()).hasSize(10);

        // Assemblies
        var assembliesDir = new File(this.outputDirectory, "assemblies");
        assertThat(assembliesDir.listFiles(new AsciidocFileFilter())).hasSize(1);
    }

    @Test
    public void testNoParentContext() throws Exception {
        final var sourceDirectory = new File(KOGITO_ASCIIDOC_FOLDER);
        var options = new String[]{"-s", sourceDirectory.getAbsolutePath(), "-o", this.outputDirectory.getAbsolutePath()};

        new CommandLine(new ExtractionRunner()).execute(options);

        var source = new File(sourceDirectory, "creating-running");
        var sourceFile = new File(source, "chap-kogito-creating-running.adoc");
        var assembliesDir = new File(this.outputDirectory, "assemblies");
        assertThat(assembliesDir.listFiles(new AsciidocFileFilter())).hasSize(1);
        var chapFile = new File(assembliesDir, "assembly-kogito-creating-running.adoc");
        assertThat(chapFile.exists()).isTrue();
        assertThat(!(Files.readString(sourceFile.toPath())).contains("preprocess::context[:parent-context: {context}]"));
        assertThat(!(Files.readString(chapFile.toPath())).contains("preprocess::context[:parent-context: {context}]"));
    }

    @Test
    @Disabled("Removing for now, issue #61")
    public void testParentContext() throws Exception {
        final var sourceDirectory = new File("./examples/kogito/parent-context");
        var options = new String[]{"-s", sourceDirectory.getAbsolutePath(), "-o", this.outputDirectory.getAbsolutePath()};

        new CommandLine(new ExtractionRunner()).execute(options);

        var chap = outputDirectory.toPath().resolve("assemblies")
                .resolve("assembly-kogito-configuring.adoc");

        assertThat(Files.readString(chap)).contains("ifdef::context[:parent-context: {context}]");
    }

    @Test
    public void testPreambleIncluded() throws Exception {
        final var sourceDirectory = new File("./examples/kogito/preamble-test");
        var options = new String[]{"-s", sourceDirectory.getAbsolutePath(),
                "-o", this.outputDirectory.getAbsolutePath(),
                "-a", "KOGITO-ENT=true"
        };

        new CommandLine(new ExtractionRunner()).execute(options);

        var chap = outputDirectory.toPath().resolve("assemblies")
                .resolve("assembly-kogito-creating-running.adoc");

        assertThat(outputDirectory.toPath().resolve("modules").resolve("preamble-test")
                .resolve("unknown-chap-kogito-creating-running.adoc")).doesNotExist();
    }

    @Test
    public void testPreambleIncludeRefs() throws Exception {
        var sourceDir = new File("src/test/resources/docs/preamble-include");
        var options = new String[]{"-s", sourceDir.getAbsolutePath(),
                "-o", this.outputDirectory.getAbsolutePath(),
                "-a", "KOGITO-ENT=true"
        };

        new CommandLine(new ExtractionRunner()).execute(options);

        var splitAssembly = Files.readString(outputDirectory.toPath().resolve("assemblies")
                .resolve("assembly-kogito-developing-process-services.adoc"));

        assertThat(splitAssembly).doesNotContain("include::{asciidoc-dir}/creating-running/chap-kogito-creating-running.adoc[tags=ref-kogito-app-examples]");
        assertThat(splitAssembly).doesNotContain("include::modules/modules/creating-running/ref-kogito-app-examples.adoc[leveloffset=+1]");
        assertThat(splitAssembly).contains("include::modules/creating-running/ref-kogito-app-examples.adoc[leveloffset=+1]");
    }

    @Test
    public void testModuleReferenceChapFileFix() throws Exception {
        var sourceDir = new File("src/test/resources/docs/preamble-include");
        var options = new String[]{"-s", sourceDir.getAbsolutePath(),
                "-o", this.outputDirectory.getAbsolutePath(),
                "-a", "KOGITO-ENT=true"
        };

        new CommandLine(new ExtractionRunner()).execute(options);

        var splitAssembly = Files.readString(outputDirectory.toPath().resolve("modules").resolve("preamble-include")
                .resolve("proc-bpmn-model-creating.adoc"));

        assertThat(splitAssembly).doesNotContain("include::{asciidoc-dir}/decision-services/chap-kogito-using-dmn-models.adoc[tags=con-kogito-service-execution]");
        assertThat(splitAssembly).contains("include::../../modules/decision-services/con-kogito-service-execution.adoc[leveloffset=+1]");
    }

    @Test
    public void testImagesdirInOutput() throws Exception {
        final var sourceDirectory = new File("src/test/resources/docs/content-test");
        var options = new String[]{"-s", sourceDirectory.getAbsolutePath(),
                "-o", this.outputDirectory.getAbsolutePath(),
                "--pantheonV2",
        };

        new CommandLine(new ExtractionRunner()).execute(options);

        var chap = outputDirectory.toPath().resolve("assemblies")
                .resolve("assembly-one.adoc");
        var module = outputDirectory.toPath().resolve("modules").resolve("content-test").resolve("con-module-two.adoc");

        assertThat(Files.readAllLines(chap).get(2)).isEqualTo(":imagesdir: _images");
        assertThat(Files.lines(module)).containsOnlyOnce(":imagesdir: ../_images");
    }

    @Test
    @Disabled("xref needs some rework")
    public void testFullTripXrefCheck() throws Exception {
        var sourceDir = new File("src/test/resources/docs/xref-test");
        var options = new String[]{"-s", sourceDir.getAbsolutePath(),
                "-o", this.outputDirectory.getAbsolutePath(),
                "-a", "KOGITO-ENT=true"
        };

        var exitCode = new CommandLine(new ExtractionRunner()).execute(options);
        assertThat(exitCode).isEqualTo(0);

        var xrefAssembly = Files.readString(outputDirectory.toPath().resolve("assemblies")
                .resolve("assembly-xref-test.adoc"));

        assertThat(xrefAssembly).contains("Here's an inline xref include::modules/xref-test/con-test-section.adoc[leveloffset=+1]");
    }
}
