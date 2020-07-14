package com.redhat.documentation.asciidoc.cli;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import com.redhat.documentation.asciidoc.extraction.AsciidocFileFilter;
import com.redhat.documentation.asciidoc.extraction.DeletionFileVisitor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import static org.assertj.core.api.Assertions.assertThat;

class ExtractionRunnerTest {
    public static final String KOGITO_ASCIIDOC_FOLDER = "./examples/kogito/input/doc-content/src/main/asciidoc";
    private File outputDirectory;

    @BeforeAll
    static void allSetUp() {
        System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");
    }

    @BeforeEach
    void setUp() throws Exception {
        this.outputDirectory = new File("target/output-docs");

        if (this.outputDirectory.exists()) {
            tearDown(); // clean-up from a previous botched run
        }
        Files.createDirectory(this.outputDirectory.toPath());
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.walkFileTree(outputDirectory.toPath(), new DeletionFileVisitor());
    }

    @Test
//    @Disabled("bad assumptions in source document")
    void testFileContentsSourceBlock() throws Exception {
        final var sourceDirectory = new File(ExtractionRunner.class.getClassLoader().getResource("docs/content-test").toURI());
        var options = new String[]{"-s", sourceDirectory.getAbsolutePath(), "-o", this.outputDirectory.getAbsolutePath()};

        var exitCode = new CommandLine(new ExtractionRunner()).execute(options);
        assertThat(exitCode).isEqualTo(0);

        // Modules
        var modulesDir = new File(this.outputDirectory, "modules");
        assertThat(modulesDir.exists()).isTrue();
        var topicDir = new File(modulesDir, "content-test");
        assertThat(modulesDir.listFiles()).hasSize(1);
        assertThat(topicDir.exists()).isTrue();
        assertThat(topicDir.listFiles()).hasSize(2);
        var moduleFileNames = Objects.requireNonNull(topicDir.listFiles(new AsciidocFileFilter()));
        assertThat(Arrays.stream(moduleFileNames).map(File::getName).collect(Collectors.toList()))
                .containsExactlyInAnyOrder("proc-module-one.adoc", "con-module-two.adoc");

        // Assemblies
        var assembliesDir = new File(this.outputDirectory, "assemblies");
        assertThat(assembliesDir.listFiles(new AsciidocFileFilter())).hasSize(1);
        final var assemblyFile = Objects.requireNonNull(assembliesDir.listFiles(new AsciidocFileFilter()))[0];
        assertThat(assemblyFile.getName()).isEqualTo("assembly-assembly-one.adoc");
    }

    @Test
    public void testDocTeamExample() throws Exception {
        final var sourceDirectory = new File("./examples/sample/input");
        var options = new String[]{"-s", sourceDirectory.getAbsolutePath(), "-o", this.outputDirectory.getAbsolutePath()};

        var exitCode = new CommandLine(new ExtractionRunner()).execute(options);
        assertThat(exitCode).isEqualTo(0);

        // Modules
        var modulesDir = new File(this.outputDirectory, "modules");
        assertThat(modulesDir.exists()).isTrue();
        assertThat(modulesDir.listFiles()).hasSize(1);
        var topicDir = new File(modulesDir, "input");
        assertThat(topicDir.exists()).isTrue();
        assertThat(topicDir.listFiles()).hasSize(6);
        // Assemblies
        var assembliesDir = new File(this.outputDirectory, "assemblies");
        assertThat(assembliesDir.listFiles(new AsciidocFileFilter())).hasSize(1);
    }

    @Test
    public void testKogitoCreatingExample() throws Exception {
        final var sourceDirectory = new File(KOGITO_ASCIIDOC_FOLDER);
        var options = new String[]{"-s", sourceDirectory.getAbsolutePath(), "-o", this.outputDirectory.getAbsolutePath(), "-i", "index.adoc"};

        var exitCode = new CommandLine(new ExtractionRunner()).execute(options);
        assertThat(exitCode).isEqualTo(0);

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
        final var sourceDirectory = new File("./examples/kogito/input");

        var options = new String[]{"-s", sourceDirectory.getAbsolutePath(), "-o", this.outputDirectory.getAbsolutePath()};

        var exitCode = new CommandLine(new ExtractionRunner()).execute(options);
        assertThat(exitCode).isEqualTo(0);

        var artifactsDir = new File(outputDirectory, "_artifacts");
        var imagesDir = new File(outputDirectory, "_images");

        assertThat(artifactsDir.exists()).isTrue();
        assertThat(imagesDir.exists()).isTrue();
    }

    @Test
    public void testSymlinkCreationUnderAssemblies() throws Exception {
        final var sourceDirectory = new File("./examples/kogito/input");
        var options = new String[]{"-s", sourceDirectory.getAbsolutePath(), "-o", this.outputDirectory.getAbsolutePath()};

        var exitCode = new CommandLine(new ExtractionRunner()).execute(options);
        assertThat(exitCode).isEqualTo(0);
        assertThat(Files.exists(Paths.get(this.outputDirectory.toString(), "assemblies", "modules"))).isTrue();
        assertThat(Files.isSymbolicLink(Paths.get(this.outputDirectory.toString(), "assemblies", "modules"))).isTrue();

        assertThat(Files.exists(Paths.get(this.outputDirectory.toString(), "assemblies", "_images"))).isTrue();
        assertThat(Files.isSymbolicLink(Paths.get(this.outputDirectory.toString(), "assemblies", "_images"))).isTrue();

        assertThat(Files.exists(Paths.get(this.outputDirectory.toString(), "assemblies", "_artifacts"))).isTrue();
        assertThat(Files.isSymbolicLink(Paths.get(this.outputDirectory.toString(), "assemblies", "_artifacts"))).isTrue();
    }

    @Test
    public void testTitleDirectoryContents() throws Exception {
        final var sourceDirectory = new File("./examples/kogito/input");
        var options = new String[]{"-s", sourceDirectory.getAbsolutePath(), "-o", this.outputDirectory.getAbsolutePath()};

        var exitCode = new CommandLine(new ExtractionRunner()).execute(options);
        assertThat(exitCode).isEqualTo(0);
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
    }

    @Test
    public void testAssemblyFalseOutput() throws Exception {
        final var sourceDirectory = new File("./src/test/resources/docs/no-assembly");
        var options = new String[]{"-s", sourceDirectory.getAbsolutePath(), "-o", this.outputDirectory.getAbsolutePath()};

        var exitCode = new CommandLine(new ExtractionRunner()).execute(options);
        assertThat(exitCode).isEqualTo(0);
        final File assembliesDir = new File(this.outputDirectory, "assemblies");
        assertThat(assembliesDir.list()).containsOnly("modules");

        // Modules
        final File modulesDir = new File(this.outputDirectory, "modules");
        assertThat(modulesDir.list()).isNotEmpty();
    }

    // Test for Issue #60
    @Test
    public void testAdditionalResources() throws Exception {
        final var sourceDirectory = new File("./examples/kogito/additional_resources");
        var options = new String[]{"-s", sourceDirectory.getAbsolutePath(),
                "-o", this.outputDirectory.getAbsolutePath(),
                "-a", "KOGITO-ENT=true"
        };

        var exitCode = new CommandLine(new ExtractionRunner()).execute(options);
        assertThat(exitCode).isEqualTo(0);
        var chap = outputDirectory.toPath().resolve("assemblies")
                .resolve("assembly-kogito-developing-decision-services.adoc");

        assertThat(Files.readString(chap)).contains("== Additional resources");
    }

    @Test
    public void testIgnoreFiles() throws Exception {
        final var sourceDirectory = new File("./examples/kogito/ignore_files");
        var options = new String[]{"-s", sourceDirectory.getAbsolutePath(),
                "-o", this.outputDirectory.getAbsolutePath(),
                "-i", "index.adoc"
        };

        var exitCode = new CommandLine(new ExtractionRunner()).execute(options);
        assertThat(exitCode).isEqualTo(0);
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

        var exitCode = new CommandLine(new ExtractionRunner()).execute(options);
        assertThat(exitCode).isEqualTo(0);

        var source= new File(sourceDirectory, "creating-running");
        var sourceFile = new File(source, "chap-kogito-creating-running.adoc");
        var assembliesDir = new File(this.outputDirectory, "assemblies");
        assertThat(assembliesDir.listFiles(new AsciidocFileFilter())).hasSize(1);
        var chapFile= new File(assembliesDir, "assembly-kogito-creating-running.adoc");
        assertThat(chapFile.exists()).isTrue();
        assertThat(!(Files.readString(sourceFile.toPath())).contains("ifdef::context[:parent-context: {context}]"));
        assertThat(!(Files.readString(chapFile.toPath())).contains("ifdef::context[:parent-context: {context}]"));
    }

    @Test
    public void testParentContext() throws Exception {
        final var sourceDirectory = new File("./examples/kogito/parent-context");
        var options = new String[]{"-s", sourceDirectory.getAbsolutePath(), "-o", this.outputDirectory.getAbsolutePath()};

        var exitCode = new CommandLine(new ExtractionRunner()).execute(options);
        assertThat(exitCode).isEqualTo(0);

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

        var exitCode = new CommandLine(new ExtractionRunner()).execute(options);
        assertThat(exitCode).isEqualTo(0);

        var chap = outputDirectory.toPath().resolve("assemblies")
                .resolve("assembly-kogito-creating-running.adoc");

        assertThat(outputDirectory.toPath().resolve("modules").resolve("preamble-test")
                .resolve("unknown-chap-kogito-creating-running.adoc")).doesNotExist();
    }
}
