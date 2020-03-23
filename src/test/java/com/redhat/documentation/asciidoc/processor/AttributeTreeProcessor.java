package com.redhat.documentation.asciidoc.processor;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Treeprocessor;
import org.asciidoctor.jruby.ast.impl.SectionImpl;

public class AttributeTreeProcessor extends Treeprocessor {

    @Override
    public Document process(Document document) {
        var section = document.getBlocks().get(0);
        var block = section.getBlocks().get(0);
        // WARNING: Dropping to internal API, potential for future break
        var propTitle = ((SectionImpl) section).getProperty("@title");

        var blockAttributes = block.getAttributes();
        var hasAttrib = block.hasAttribute("my-attribute");
        var attributeKeys = blockAttributes.keySet();

        return document;
    }
}
