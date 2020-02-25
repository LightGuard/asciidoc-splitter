package com.redhat.documentation.asciidoc;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;

public class Configuration {
    private File sourceDirectory;
    private File outputDirectory;

    public Configuration(File sourceDirectory, File outputDirectory) {
        assert sourceDirectory.isDirectory();
        assert outputDirectory.isDirectory();

        this.sourceDirectory = sourceDirectory;
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

    public File getSourceDirectory() {
        return sourceDirectory;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }
}
