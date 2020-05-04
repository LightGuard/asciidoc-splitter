package com.redhat.documentation.asciidoc.extraction.model;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Handles git repository target locations
 */
public class LocalDirectoryPushableLocation implements PushableLocation {
    private final File directory;

    public LocalDirectoryPushableLocation(File directory) {
        this.directory = directory;
    }

    /**
     * Gets the directory path of pushable location
     * @return pushable location directory path
     */
    @Override
    public Path getDirectoryPath() {
        return directory.toPath();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LocalDirectoryPushableLocation that = (LocalDirectoryPushableLocation) o;
        return Objects.equals(directory, that.directory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(directory);
    }

    @Override
    public String toString() {
        return "LocalDirectoryPushableLocation{" +
                "directory=" + directory +
                '}';
    }
}
