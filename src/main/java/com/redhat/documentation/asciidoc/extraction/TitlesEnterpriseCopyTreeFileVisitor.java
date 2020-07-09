package com.redhat.documentation.asciidoc.extraction;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

import com.redhat.documentation.asciidoc.Util;

/**
 * A specific instance of CopyTreeFileVisitor used for the "titles-enterprise" directory.
 */
public class TitlesEnterpriseCopyTreeFileVisitor extends CopyTreeFileVisitor {
    public TitlesEnterpriseCopyTreeFileVisitor(Path sourcePath, Path targetPath) {
        super(sourcePath, targetPath);
    }

    public TitlesEnterpriseCopyTreeFileVisitor(Path sourcePath, Path targetPath, String filePatternToCopy) {
        super(sourcePath, targetPath, filePatternToCopy);
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        Objects.requireNonNull(dir);
        Objects.requireNonNull(attrs);

        var newDirectory = targetPath.resolve(sourcePath.getParent().relativize(dir)).normalize();

        // If the dir is a symlink, create the link and move on
        if (dir.toString().contains(Extractor.TITLES_ENTERPRISE)) { // We're inside the titles-enterprise directory
            if (Files.isSymbolicLink(dir)) {
                if (dir.getFileName().toString().equals("doc-content")) { // We don't need doc-content, but do need assemblies
                    var parentDir = dir.getParent().getFileName();
                    var assemblies = targetPath.resolve(Extractor.TITLES_ENTERPRISE).resolve(parentDir).resolve("assemblies");

                    // Create symlink for assemblies and modules
                    Files.createSymbolicLink(assemblies,
                            assemblies.getParent().relativize(targetPath.resolve("assemblies")));

                    return FileVisitResult.SKIP_SUBTREE;
                }

                Files.createSymbolicLink(newDirectory, Files.readSymbolicLink(dir));
                return FileVisitResult.SKIP_SUBTREE;
            }
        }

        return super.preVisitDirectory(dir, attrs);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        // if master.adoc
        //  read file
        //  fix includes
        //  write to new location
        // else
        if (file.getFileName().toString().equals("master.adoc")) {
            var lines = Files.readString(file);
            var newFile = targetPath.resolve(Extractor.TITLES_ENTERPRISE)
                                    .resolve(file.getParent().getFileName())
                                    .resolve(file.getFileName());
            Files.writeString(newFile, Util.fixIncludes(lines));
            return FileVisitResult.CONTINUE;
        }
        return super.visitFile(file, attrs);
    }
}
