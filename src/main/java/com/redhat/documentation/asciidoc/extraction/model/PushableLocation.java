package com.redhat.documentation.asciidoc.extraction.model;

import java.nio.file.Path;

/**
 * A specialized {@link Location} that allows for files to be "pushed", like a source repo.
 */
public interface PushableLocation extends Location {
    /**
     * Save action for files.
     * This is mostly used in the concept of a source repository.
     */
    void push();

    static PushableLocation locationWrapper(Location loc, Runnable pushAction) {
        return new PushableLocation() {
            @Override
            public void push() {
                pushAction.run();
            }

            @Override
            public Path getDirectoryPath() {
                return loc.getDirectoryPath();
            }
        };
    }
}
