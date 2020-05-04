package com.redhat.documentation.asciidoc.cli;

import com.redhat.documentation.asciidoc.extraction.AsciidocFileFilter;
import com.redhat.documentation.asciidoc.extraction.DeletionFileVisitor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class ExtractionRunnerTest {
    public static final String TEST_GIT_REPO = "https://github.com/manaswinidas/Docs-symlink.git";
    private File outputDirectory;

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
    void testShouldPrepareDocsWhenSourceIsLocalDirAndTargetIsGitRepo() throws URISyntaxException, IOException, GitAPIException {
        final var sourceDirectory = new File(Objects.requireNonNull(ExtractionRunner.class
                .getClassLoader().getResource("docs/content-test")).toURI());

        String[] options = {"-s", sourceDirectory.getAbsolutePath(), "-or", TEST_GIT_REPO, "-ob", "test"};
        var exitCode = new CommandLine(new ExtractionRunner()).execute(options);
        assertThat(exitCode).isEqualTo(0);

        var tmp = Files.createTempDirectory("extraction-runner-tests");
        Git.cloneRepository()
                .setURI(TEST_GIT_REPO)
                .setBranch("test")
                .setDirectory(tmp.toFile())
                .call();

        assertThat(Files.isDirectory(tmp.resolve("modules"))).isTrue();
        assertThat(Files.isRegularFile(tmp.resolve("modules/con-module-two.adoc"))).isTrue();
        assertThat(Files.isRegularFile(tmp.resolve("modules/proc-module-one.adoc"))).isTrue();
        assertThat(Files.isDirectory(tmp.resolve("assemblies"))).isTrue();
        assertThat(Files.isRegularFile(tmp.resolve("assemblies/assembly-assembly-one.adoc"))).isTrue();
    }

    //    @Test
//    @Disabled("bad assumptions in source document")
//    void testRun() throws Exception {
//        final var sourceDirectory = new File(ExtractionRunner.class.getClassLoader().getResource("docs/basic").toURI());
//        var options = new String[] {"-s", sourceDirectory.getAbsolutePath(), "-o", this.outputDirectory.getAbsolutePath()};
//
//        var exitCode = new CommandLine(new ExtractionRunner()).execute(options);
//        assertThat(exitCode).isEqualTo(0);
//
//        // Modules
//        var modulesDir = new File(this.outputDirectory, "modules");
//        assertThat(modulesDir.exists()).isTrue();
//        assertThat(modulesDir.listFiles()).hasSize(4);
//
//        // Assemblies
//        var assembliesDir = new File(this.outputDirectory, "assemblies");
//        assertThat(assembliesDir.listFiles(new AsciidocFileFilter())).hasSize(2);
//        assertThat(Arrays.stream(Objects.requireNonNull(assembliesDir.listFiles(new AsciidocFileFilter())))
//                            .map(File::getName)
//                            .collect(Collectors.toList()))
//                .containsExactlyInAnyOrder("assembly-1.adoc", "assembly-2.adoc");
//    }
//
//    @Test
//    @Disabled("bad assumptions in source document")
//    void testRunRealWorld() throws Exception {
//        final var sourceDirectory = new File(ExtractionRunner.class.getClassLoader().getResource("docs/real-world").toURI());
//        var options = new String[] {"-s", sourceDirectory.getAbsolutePath(), "-o", this.outputDirectory.getAbsolutePath()};
//
//        var exitCode = new CommandLine(new ExtractionRunner()).execute(options);
//        assertThat(exitCode).isEqualTo(0);
//
//        // Modules
//        var modulesDir = new File(this.outputDirectory, "modules");
//        assertThat(modulesDir.exists()).isTrue();
//        assertThat(modulesDir.listFiles()).hasSize(3);
//
//        // Assemblies
//        var assembliesDir = new File(this.outputDirectory, "assemblies");
//        assertThat(assembliesDir.listFiles(new AsciidocFileFilter())).hasSize(7);
//    }
//
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
        assertThat(modulesDir.listFiles()).hasSize(2);
        var moduleFileNames = Objects.requireNonNull(modulesDir.listFiles(new AsciidocFileFilter()));
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
        assertThat(modulesDir.listFiles()).hasSize(6);

        // Assemblies
        var assembliesDir = new File(this.outputDirectory, "assemblies");
        assertThat(assembliesDir.listFiles(new AsciidocFileFilter())).hasSize(1);
    }

    @Test
    public void testKogitoCreatingExample() throws Exception {
        final var sourceDirectory = new File("./examples/kogito/input");
        var options = new String[]{"-s", sourceDirectory.getAbsolutePath(), "-o", this.outputDirectory.getAbsolutePath()};

        var exitCode = new CommandLine(new ExtractionRunner()).execute(options);
        assertThat(exitCode).isEqualTo(0);

        // Modules
        var modulesDir = new File(this.outputDirectory, "modules");
        assertThat(modulesDir.exists()).isTrue();
        assertThat(modulesDir.listFiles()).hasSize(10);

        // Assemblies
        var assembliesDir = new File(this.outputDirectory, "assemblies");
        assertThat(assembliesDir.listFiles(new AsciidocFileFilter())).hasSize(1);
    }
}

