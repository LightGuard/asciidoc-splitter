package io.github.lightguard.documentation.asciidoc.extraction;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static io.github.lightguard.documentation.asciidoc.extraction.Extractor.TITLES_ENTERPRISE;

public class AsciidocChapFileVisitor extends SimpleFileVisitor<Path> {
    private final List<File> adocFiles;
    private final List<File> ignoredFiles;
    private final Logger logger;

    public AsciidocChapFileVisitor(Collection<File> ignoredFiles) {
        this.adocFiles = new ArrayList<>();
        this.ignoredFiles = new ArrayList<>(ignoredFiles);
        this.logger = LogManager.getLogManager().getLogger("");

        this.logger.fine("Ignored files/directories: " + ignoredFiles);
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        Objects.requireNonNull(dir);
        Objects.requireNonNull(attrs);

        if (this.ignoredFiles.contains(new File(dir.toFile().getName()))) {
            this.logger.fine("Ignoring directory (as asked): " + dir);
            return FileVisitResult.SKIP_SUBTREE;
        }

        if (dir.toFile().toString().contains(TITLES_ENTERPRISE)) {
            this.logger.fine("Ignoring directory (title_enterprise dir): " + dir);
            return FileVisitResult.SKIP_SUBTREE;
        }

        return super.preVisitDirectory(dir, attrs);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        this.logger.fine("Found file: " + file);
        if (this.ignoredFiles.contains(new File(file.toFile().getName()))) {
            this.logger.fine("Ignoring file (as asked): " + file);
            return FileVisitResult.CONTINUE;
        }

        if (!file.getFileName().toString().endsWith(".adoc")
                && !file.getFileName().toString().endsWith(".ad")
                && !file.getFileName().toString().endsWith(".asc")) {
            this.logger.fine("Ignoring file (non-adoc file): " + file);
            return FileVisitResult.CONTINUE;
        }

        // Skip symlinks
        if (Files.isSymbolicLink(file)) {
            this.logger.fine("Ignoring symlink: " + file);
            return FileVisitResult.CONTINUE;
        }

        this.adocFiles.add(file.toFile());
        return FileVisitResult.CONTINUE;
    }

    public List<File> getAdocFiles() {
        return Collections.unmodifiableList(adocFiles);
    }
}
