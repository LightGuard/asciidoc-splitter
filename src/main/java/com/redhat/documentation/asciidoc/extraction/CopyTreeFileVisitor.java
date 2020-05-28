package com.redhat.documentation.asciidoc.extraction;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class CopyTreeFileVisitor extends SimpleFileVisitor<Path> {
    private final Path targetPath;
    private final Path sourcePath;

    public CopyTreeFileVisitor(Path targetPath, Path sourcePath) {
        super();
        this.targetPath = targetPath;
        this.sourcePath = sourcePath;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        Objects.requireNonNull(dir);
        Objects.requireNonNull(attrs);

        CopyOption[] options = new CopyOption[]{COPY_ATTRIBUTES};

        var newDirectory = targetPath.resolve(sourcePath.relativize(dir));

        try {
            Files.copy(dir, newDirectory, options);
        } catch (FileAlreadyExistsException e) {
            // Ignore, doesn't matter
        } catch (IOException e) {
            // TODO We need to do this better
            System.err.println(e.getMessage());
            return FileVisitResult.TERMINATE;
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        CopyOption[] options = new CopyOption[] { COPY_ATTRIBUTES, REPLACE_EXISTING };

        try {
            Files.copy(file, targetPath.resolve(sourcePath.relativize(file)), options);
        } catch (IOException x) {
            System.err.format("Unable to copy: %s: %s%n", file, x);
        }
        return FileVisitResult.CONTINUE;
    }
}
