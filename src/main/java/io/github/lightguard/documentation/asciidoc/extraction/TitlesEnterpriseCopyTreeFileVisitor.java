package io.github.lightguard.documentation.asciidoc.extraction;

import io.github.lightguard.documentation.asciidoc.Util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

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
                    // Location of "titles-enterprise"
                    final int start = dir.getParent().toString().indexOf(Extractor.TITLES_ENTERPRISE);
                    var newDir = targetPath.resolve(dir.getParent().toString().substring(start));

                    var assemblies = targetPath.resolve(newDir).resolve("assemblies");

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

            // Location of "titles-enterprise"
            final int start = file.toString().indexOf(Extractor.TITLES_ENTERPRISE);
            var newFile = targetPath.resolve(file.toString().substring(start));

            // Create the directory structure if we need to
            if (!Files.exists(newFile.getParent())) {
                Files.createDirectories(newFile.getParent());
            }

            Files.writeString(newFile, Util.fixIncludes(lines, false));
            return FileVisitResult.CONTINUE;
        }
        return super.visitFile(file, attrs);
    }
}
