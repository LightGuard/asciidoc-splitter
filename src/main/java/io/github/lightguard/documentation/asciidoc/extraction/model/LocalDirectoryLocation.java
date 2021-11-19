package io.github.lightguard.documentation.asciidoc.extraction.model;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

/**
 * A system local directory, backed by a {@link File}.
 */
public class LocalDirectoryLocation implements Location {
    private final File directory;

    public LocalDirectoryLocation(File directory) {
        this.directory = directory.getAbsoluteFile();
    }

    @Override
    public String toString() {
        return "LocalDirectoryLocation{" +
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
        LocalDirectoryLocation that = (LocalDirectoryLocation) o;
        return Objects.equals(directory, that.directory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(directory);
    }

    /**
     * Gets the directory path of location
     * @return location directory path
     */
    @Override
    public Path getDirectoryPath() {
        return this.directory.toPath();
    }
}
