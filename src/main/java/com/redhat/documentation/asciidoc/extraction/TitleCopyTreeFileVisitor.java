package com.redhat.documentation.asciidoc.extraction;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TitleCopyTreeFileVisitor extends CopyTreeFileVisitor {
    public TitleCopyTreeFileVisitor(Path targetPath, Path sourcePath) {
        super(Paths.get(targetPath.toString(), Extractor.TITLES_ENTERPRISE), sourcePath);
    }
}
