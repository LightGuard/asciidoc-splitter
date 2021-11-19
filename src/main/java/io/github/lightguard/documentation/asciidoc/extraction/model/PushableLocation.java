package io.github.lightguard.documentation.asciidoc.extraction.model;

import java.nio.file.Path;
import java.util.Objects;

/**
 * A specialized {@link Location} that allows for files to be "pushed", like a source repo.
 */
public interface PushableLocation extends Location, AutoCloseable {
    /**
     * Save action for files.
     * This is mostly used in the concept of a source repository.
     */
    void push();

    static PushableLocation locationWrapper(Location loc, Runnable pushAction) {
        if (Objects.isNull(loc)) {
            throw new IllegalArgumentException("loc must be non-null");
        }

        if (Objects.isNull(pushAction)) {
            throw new IllegalArgumentException("pushAction must be non-null");
        }

        return new PushableLocation() {
            @Override
            public void close() throws Exception {
                // Empty impl, nothing to actually close
            }

            @Override
            public void push() {
                pushAction.run();
            }

            @Override
            public Path getDirectoryPath() {
                return loc.getDirectoryPath();
            }

            @Override
            public String toString() {
                return "PushableLocationWrapper{" +
                       "directory='" + loc.getDirectoryPath() + '\'' +
                       '}';
            }
        };
    }
}
