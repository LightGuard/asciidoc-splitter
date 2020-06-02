package com.redhat.documentation.asciidoc.extraction;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

public class TitleCopyTreeFileVisitor extends CopyTreeFileVisitor {
    public TitleCopyTreeFileVisitor(Path targetPath, Path sourcePath) {
        super(Paths.get(targetPath.toString(), Extractor.TITLES_ENTERPRISE), sourcePath);
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        // If dir is symlink, drop, we shouldn't need it in this instance.
        if (Files.isSymbolicLink(dir)) {
            return FileVisitResult.SKIP_SUBTREE;
        }

        // continue with this normally
        return super.preVisitDirectory(dir, attrs);
    }
}
