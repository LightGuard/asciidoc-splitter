package com.redhat.documentation.asciidoc;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.DescriptionList;
import org.asciidoctor.ast.List;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Table;

public class SourceExtractor {
    private ContentNode node;
    private StringBuilder source;

    public SourceExtractor(ContentNode node) {
        this.node = node;
        this.source = new StringBuilder();

        if (node instanceof Section)
            extractSection();

        if (node instanceof Block) {
            switch (node.getContext()) {
                case "paragraph":
                    extractParagraph();
                    break;
                // TODO: Add more
            }
        }

    }

    public String getSource() {
        return source.toString();
    }

    // TODO: Block types:
    //        Open: --
    //        Quote && Verse: ____
    //        sidebar: ****
    //        Fenced: ```
    //        Literal: ....
    //        Passthrough && Stem: ++++
    //        Table: |===
    //        Admonition: ====
    //        Source && Listing: ----

    private void extractParagraph() {
        extractMetadata();

        source.append(((Block) node).getSource());
    }

    private void extractSection() {
        var section = (Section) node;
        var id = section.getId();

        // If there isn't an explicit id, it starts with an _
        if (id.startsWith("_")) {
            // Don't use the first character (an underscore) and replace underscore with hyphen
            id = id.substring(1).replaceAll("_", "-");
        }
        var context = id;
        var title = section.getTitle();
        var level = section.getLevel() < 2 ? 1 : section.getLevel() - 1;

        extractMetadata();
        source.append("\n").append("=".repeat(Math.max(0, level))).append(" ").append(title).append("\n");
        source.append(":context: ").append(context);
    }

    private void extractListing() {
        var block = (Block) node;
        // Regex to look for the numbered callout in regular source and xml

        // TODO: add the attributes
        // TODO: add title
        extractMetadata();

        var hasCallout = block.getSource().matches("<(!--)?(\\d+|\\.)(--)?>");
        if ((hasCallout)) {
            source.append("\n");
        } else {
            source.append("\n\n");
        }
    }

    private void extractCalloutList() {
        var list = (List) node;
    }

    private void extractTable() {
        var table = (Table) node;
    }

    private void extractList() {
        var list = (List) node;
    }

    private void extractDescriptionList() {
        var descriptionList = (DescriptionList) node;
    }

    // TODO: context - Listing (need the attributes [style, language, title, subs])
    //                 A listing is (in this context) a block of code
    //                 If there are call outs the next block will be a listing block

    private boolean hasRoles() {
        return !node.getRoles().isEmpty();
    }

    private boolean hasAttributes() {
        return !node.getAttributes().isEmpty();
    }

    private boolean hasStyle() {
        return (node instanceof StructuralNode) && ((StructuralNode) node).getStyle() != null;
    }

    /**
     * Pulls the id, attributes, style, and roles from the node into the proper syntax
     */
    private void extractMetadata() {
        var id = node.getId();
        var hasId = id != null;
        var hasAttributes = hasAttributes();
        var hasRoles = hasRoles();
        var hasStyle = hasStyle();

        if (hasId || hasRoles || hasAttributes || hasStyle) {
            source.append("[");

            if (hasId) {
                // If there isn't an explicit id for a section, it starts with an _
                if (id.startsWith("_")) {
                    // Don't use the first character (an underscore) and replace underscore with hyphen
                    id = id.substring(1).replaceAll("_", "-");
                }
                var idWithoutContext = id.split("_")[0];
                source.append("id=\"").append(idWithoutContext).append("_{context}\"");
            }

            if (hasStyle)
                source.append(" ").append(((StructuralNode) node).getStyle());

            if (hasAttributes) {
                // TODO: Attributes
            }

            if (hasRoles)
                source.append(" role=\"").append(String.join(",", node.getRoles())).append("\"");

            source.append("]");
        }
    }
}
