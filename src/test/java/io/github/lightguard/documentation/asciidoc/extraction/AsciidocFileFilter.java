package io.github.lightguard.documentation.asciidoc.extraction;

import java.io.File;
import java.io.FileFilter;

public class AsciidocFileFilter implements FileFilter {
    @Override
    public boolean accept(File pathname) {
        if (pathname.isDirectory())
            return false;

        var filename = pathname.getName();
        return filename.endsWith(".adoc") ||
                filename.endsWith(".ad") ||
                filename.endsWith(".asciidoc");
    }
}
