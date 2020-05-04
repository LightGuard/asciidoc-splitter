package com.redhat.documentation.asciidoc.extraction.model;

import java.nio.file.Path;

/**
 * Handles pushable locations
 */
public interface PushableLocation extends Location{
    Path getDirectoryPath();

    default void push(){

    }
}
