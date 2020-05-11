package com.redhat.documentation.asciidoc.extraction.model;

import java.nio.file.Path;

/**
 * Handles source locations
 */
public interface Location {
    Path getDirectoryPath();
}
