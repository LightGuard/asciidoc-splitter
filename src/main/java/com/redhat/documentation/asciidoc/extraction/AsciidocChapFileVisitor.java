package com.redhat.documentation.asciidoc.extraction;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (this.ignoredFiles.contains(new File(file.toFile().getName()))) {
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
