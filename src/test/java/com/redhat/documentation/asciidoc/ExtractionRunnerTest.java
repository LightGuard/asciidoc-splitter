package com.redhat.documentation.asciidoc;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExtractionRunnerTest {
    private Configuration config;
    private File outputDirectory;

    @BeforeEach
    void setUp() throws Exception {
        this.outputDirectory = new File("target/output-docs");

        if (!this.outputDirectory.exists())
            Files.createDirectory(this.outputDirectory.toPath());

        final var sourceDirectory = new File(SectionTreeProcessor.class.getClassLoader().getResource("docs").toURI());

        this.config = new Configuration(sourceDirectory, outputDirectory);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.walkFileTree(outputDirectory.toPath(), new DeletionFileVisitor());
    }

    @Test
    void testRun() throws Exception {
        var cut = new ExtractionRunner(this.config);
        cut.run();

        // Modules
        var modulesDir = new File(this.outputDirectory, "modules");
        assertThat(modulesDir.exists()).isTrue();
        assertThat(modulesDir.listFiles()).hasSize(4);

        // Assemblies
        assertThat(this.outputDirectory.listFiles(new AsciidocFileFilter())).hasSize(2);
        assertThat(Arrays.stream(Objects.requireNonNull(this.outputDirectory.listFiles(new AsciidocFileFilter())))
                            .map(File::getName)
                            .collect(Collectors.toList()))
                .containsExactly("assembly-1.adoc", "assembly-2.adoc");
    }

    // TODO: I need a test to check the contents of the file(s)
}

