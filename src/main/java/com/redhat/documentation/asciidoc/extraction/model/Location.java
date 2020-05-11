package com.redhat.documentation.asciidoc.extraction.model;

import java.nio.file.Path;

/**
 * A Location of files.
 */
public interface Location {
    Path getDirectoryPath();
}
