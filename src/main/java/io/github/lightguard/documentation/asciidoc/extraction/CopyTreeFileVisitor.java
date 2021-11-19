package io.github.lightguard.documentation.asciidoc.extraction;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.Stack;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Basic FileVisitor specialization which copies files.
 */
public class CopyTreeFileVisitor extends SimpleFileVisitor<Path> {
    final Path targetPath;
    final Path sourcePath;
    final PathMatcher pathMatcher;
    private final Logger logger;
    private final Stack<Path> directories;

    /**
     * Builds a new instance which copies all files using "glob:*.*" for the matcher.
     * See {@link FileSystem#getPathMatcher} for more information
     *
     * @param sourcePath source of the copy
     * @param targetPath target to copy to
     */
    public CopyTreeFileVisitor(Path sourcePath, Path targetPath) {
        this(sourcePath, targetPath, "glob:**");
    }

    /**
     * Builds a new instance using the supplied value for the path matcher.
     *
     * @param sourcePath        source of the copy
     * @param targetPath        target to copy to
     * @param filePatternToCopy passed to the PathMatcher builder, see {@link FileSystem#getPathMatcher} for more information.
     */
    public CopyTreeFileVisitor(Path sourcePath, Path targetPath, String filePatternToCopy) {
        if (!filePatternToCopy.startsWith("glob:") && !filePatternToCopy.startsWith("regex:")) {
            throw new IllegalArgumentException("Illegal file pattern: " + filePatternToCopy);
        }

        this.targetPath = targetPath;
        this.sourcePath = sourcePath;
        this.logger = LogManager.getLogManager().getLogger("");
        this.pathMatcher = FileSystems.getDefault().getPathMatcher(filePatternToCopy);
        this.directories = new Stack<>();
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        Objects.requireNonNull(dir);
        Objects.requireNonNull(attrs);


        CopyOption[] options = new CopyOption[]{COPY_ATTRIBUTES};

        var newDirectory = targetPath.resolve(sourcePath.getParent().relativize(dir)).normalize();

        // If the dir is a symlink, create the link and move on
        if (Files.isSymbolicLink(dir)) {
            Files.createSymbolicLink(newDirectory, Files.readSymbolicLink(dir));
            return FileVisitResult.SKIP_SUBTREE;
        }

        try {
            if (!newDirectory.toFile().exists()) {
                this.logger.fine("Creating directory: " + newDirectory);
                Files.createDirectory(newDirectory);
            }

            this.directories.push(newDirectory);
        } catch (FileAlreadyExistsException e) {
            // Ignore, doesn't matter
        } catch (IOException e) {
            logger.severe("Error copying directory '" + newDirectory + "' : " + e.getMessage());
            return FileVisitResult.TERMINATE;
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        this.directories.pop();
        return super.postVisitDirectory(dir, exc);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

        if (pathMatcher.matches(file)) {
            try {
                this.logger.fine("Copying file: '" + file + "' to new directory: '" + targetPath.resolve(file.getParent()) + "'");
                CopyOption[] options = new CopyOption[]{COPY_ATTRIBUTES, REPLACE_EXISTING};

                Files.copy(file, directories.peek().resolve(file.getFileName()), options);
            } catch (IOException x) {
                logger.severe(String.format("Unable to copy: %s: %s%n", file, x));
            }
        }
        return FileVisitResult.CONTINUE;
    }
}
