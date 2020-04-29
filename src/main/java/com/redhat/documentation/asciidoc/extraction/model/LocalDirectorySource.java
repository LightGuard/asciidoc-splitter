package com.redhat.documentation.asciidoc.extraction.model;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

public class LocalDirectorySource implements Source {
    private final File directory;

    public LocalDirectorySource(File directory) {
        this.directory = directory;
    }

    @Override
    public String toString() {
        return "LocalDirectorySource{" +
                "directory='" + directory + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LocalDirectorySource that = (LocalDirectorySource) o;
        return Objects.equals(directory, that.directory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(directory);
    }


    @Override
    public Path getDirectoryPath() {
        return this.directory.toPath();
    }
}
