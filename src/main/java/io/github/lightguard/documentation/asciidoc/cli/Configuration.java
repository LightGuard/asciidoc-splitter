package io.github.lightguard.documentation.asciidoc.cli;

import java.io.File;
import java.net.URI;

/**
 * POJO for configuration pulled from the CLI.
 * Immutable.
 */
public class Configuration {
    private File sourceDirectory;
    private File outputDirectory;
    private String sourceRepo;
    private String sourceBranch;
    private String outputRepo;
    private String outputBranch;

    public Configuration(File sourceDirectory, File outputDirectory) {
        assert sourceDirectory.isDirectory();
        assert outputDirectory.isDirectory();

        this.sourceDirectory = sourceDirectory;
        this.outputDirectory = outputDirectory;
    }

    public Configuration(File sourceDirectory, String outputRepo, String outputBranch) {
        assert sourceDirectory.isDirectory();

        this.sourceDirectory = sourceDirectory;
        this.outputRepo = outputRepo;
        this.outputBranch = outputBranch;
    }

    public Configuration(String sourceRepo, String sourceBranch, File outputDirectory) {
        assert outputDirectory.isDirectory();

        this.sourceRepo = sourceRepo;
        this.sourceBranch = sourceBranch;
        this.outputDirectory = outputDirectory;
    }

    public Configuration(String sourcePathName, String outputPathName) {
        assert new File(sourcePathName).isDirectory();
        assert new File(outputPathName).isDirectory();

        this.sourceDirectory = new File(sourcePathName);
        this.outputDirectory = new File(outputPathName);
    }

    public Configuration(URI sourceDirectory, URI outputDirectory) {
        assert new File(sourceDirectory).isDirectory();
        assert new File(outputDirectory).isDirectory();

        this.sourceDirectory = new File(sourceDirectory);
        this.outputDirectory = new File(outputDirectory);
    }

    public Configuration(String sourceRepo, String sourceBranch, String outputRepo, String outputBranch) {
        this.sourceRepo = sourceRepo;
        this.sourceBranch = sourceBranch;
        this.outputRepo = outputRepo;
        this.outputBranch = outputBranch;
    }

    public File getSourceDirectory() {
        return sourceDirectory;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public String getSourceRepo() {
        return sourceRepo;
    }

    public String getSourceBranch() {
        return sourceBranch;
    }

    public String getOutputRepo() {
        return outputRepo;
    }

    public String getOutputBranch() {
        return outputBranch;
    }
}
