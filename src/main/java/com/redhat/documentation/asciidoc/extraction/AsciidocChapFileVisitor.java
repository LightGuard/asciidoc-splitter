package com.redhat.documentation.asciidoc.extraction;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import static com.redhat.documentation.asciidoc.extraction.Extractor.TITLES_ENTERPRISE;

public class AsciidocChapFileVisitor extends SimpleFileVisitor<Path> {
    private List<File> adocFiles = new ArrayList<>();
    private List<File> ignoredFiles;

    public AsciidocChapFileVisitor(Collection<File> ignoredFiles) {
        this.adocFiles = new ArrayList<>();
        ;
        this.ignoredFiles = new ArrayList<>(ignoredFiles);
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        Objects.requireNonNull(dir);
        Objects.requireNonNull(attrs);

        if (this.ignoredFiles.contains(new File(dir.toFile().getName()))) {
            return FileVisitResult.SKIP_SUBTREE;
        }

        return super.preVisitDirectory(dir, attrs);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (this.ignoredFiles.contains(new File(file.toFile().getName()))) {
            return FileVisitResult.CONTINUE;
        }

        if (!file.getFileName().toString().endsWith(".adoc")
                && !file.getFileName().toString().endsWith(".ad")
                && !file.getFileName().toString().endsWith(".asc")) {
            return FileVisitResult.CONTINUE;
        }

        // Skip symlinks
        if (Files.isSymbolicLink(file)) {
            return FileVisitResult.CONTINUE;
        }

        // Skip adoc files in the title enterprise directory
        if (file.toFile().getParent() != null && file.toFile().getParent().contains(TITLES_ENTERPRISE)) {
            return FileVisitResult.CONTINUE;
        }

        this.adocFiles.add(file.toFile());
        return FileVisitResult.CONTINUE;
    }

    public List<File> getAdocFiles() {
        return Collections.unmodifiableList(adocFiles);
    }
}
