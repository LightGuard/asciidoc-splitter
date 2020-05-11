package com.redhat.documentation.asciidoc.extraction.model;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

public class LocalDirectoryTarget implements Target {
    private final File directory;

    public LocalDirectoryTarget(File directory) {
        this.directory = directory;
    }

    @Override
    public Path getDirectoryPath() {
        return directory.toPath();
    }

    public File getDirectory() {
        return directory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LocalDirectoryTarget that = (LocalDirectoryTarget) o;
        return Objects.equals(directory, that.directory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(directory);
    }

    @Override
    public String toString() {
        return "LocalDirectoryTarget{" +
                "directory=" + directory +
                '}';
    }
}
