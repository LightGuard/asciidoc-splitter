package io.github.lightguard.documentation.asciidoc.extension;

import java.util.Objects;
import java.util.stream.Collectors;

import io.github.lightguard.documentation.asciidoc.Util;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.Treeprocessor;

public class ReplaceWithTreeProcessor extends Treeprocessor {
    private ReaderPreprocessor readerPreprocessor;

    @Override
    public Document process(Document document) {
        Objects.requireNonNull(this.readerPreprocessor, "ReaderPreProcessor must be set");

        // Small (hopefully) optimization, no point in doing this if there isn't any replace-with sections
        if (this.readerPreprocessor.getLines().stream()
                    .filter(s -> s.contains("replace-with"))
                    .collect(Collectors.toList()).size() > 0)
            document.getBlocks().forEach(this::processBlocks);
        return document;
    }

    public void setReaderPreprocessor(ReaderPreprocessor readerPreprocessor) {
        this.readerPreprocessor = readerPreprocessor;
    }

    private void processBlocks(StructuralNode node) {
        var iter = node.getBlocks().listIterator();
        while (iter.hasNext()) {
            var block = iter.next();

            if (block.getAttributes().containsKey("replace-with")) {
                if (block instanceof Block) {
                    // Get ready to play with some numbers
                    int startIndex;
                    if (block.getAttributes() == null || block.getAttributes().isEmpty()) {
                        startIndex = block.getSourceLocation().getLineNumber(); // No attributes
                    } else {
                        startIndex = block.getSourceLocation().getLineNumber() - 1; // Get the attributes line too
                    }

                    // Ending should be wherever it ends plus the attribute line;
                    var endIndex = startIndex + ((Block) block).getLines().size();
                    var newBlock = replaceWith(block);
                    iter.set(newBlock);
                    this.readerPreprocessor.updateLines(startIndex, endIndex, newBlock.getLines());
                }
            }
        }
    }

    private Block replaceWith(StructuralNode origBlock) {
        String content = "include::" +
                         origBlock.getAttributes().get("replace-with") +
                         "[" + origBlock.getAttributes().get("replace-with-params") + "]";
        content = Util.fixModuleInclude(content);
        return createBlock((StructuralNode) origBlock.getParent(), "paragraph", content);
    }
}
