package io.github.lightguard.documentation.asciidoc.extraction;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;

class TitlesEnterpriseCopyTreeFileVisitorTest {
    protected File outputDirectory;
    protected Path outputDirPath;

    @BeforeAll
    static void allSetUp() {
        System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");
    }

    @BeforeEach
    void setUp() throws Exception {
        this.outputDirectory = new File("target/output-docs");
        this.outputDirPath = this.outputDirectory.toPath();

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
    void visitFile() throws Exception {
        var sourceDir = new File("./src/test/resources/docs/issue-92");
        var cut = new TitlesEnterpriseCopyTreeFileVisitor(sourceDir.toPath(), outputDirPath);

        Files.walkFileTree(sourceDir.toPath(), EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, cut);

        var assertPath = outputDirPath.resolve("titles-enterprise/base/productName/master.adoc");
        assertThat(assertPath).exists();
    }
}