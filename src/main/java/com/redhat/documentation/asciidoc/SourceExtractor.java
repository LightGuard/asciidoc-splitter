package com.redhat.documentation.asciidoc;

import java.util.Iterator;
import java.util.StringJoiner;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.DescriptionList;
import org.asciidoctor.ast.DescriptionListEntry;
import org.asciidoctor.ast.List;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Table;

public class SourceExtractor {
    private ContentNode node;
    private StringBuilder source;

    enum BlockType {
        OPEN("--"),
        QUOTE("____"),
        VERSE("____"),
        SIDEBAR("****"),
        FENCED("```"),
        LITERAL("...."),
        PASSTHROUGH("++++"),
        STEM("++++"),
        ADMONITION("===="),
        SOURCE("----"),
        PARAGRAPH(""),
        LISTING("----");

        private String delimiter;

        BlockType(String delimiter) {
            this.delimiter = delimiter;
        }

        public String delimiter() {
            return this.delimiter;
        }
    }

    public SourceExtractor(ContentNode node) {
        this.node = node;
        this.source = new StringBuilder();

        extractMetadata();

        if (node instanceof Section)
            extractSection();

        if (node instanceof Block)
            extractBlock();

        if (node instanceof Table)
            extractTable();

        if (node instanceof List)
            extractList();

        if (node instanceof DescriptionList)
            extractDescriptionList();
    }

    public String getSource() {
        return source.toString();
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

        source
                // Metadata first
                .append("\n")
                .append("=".repeat(Math.max(0, level))).append(" ").append(title)
                .append("\n")
                .append(":context: ").append(context);
    }

    private void extractBlock() {
        var block = (Block) node;

        // the metadata comes first and does not end in a new line
        if (source.length() > 0)
            source.append("\n");

        // depending on the type of block, there are different delimiters
        // This should work for all different types of blocks
        var delimiter = BlockType.valueOf(block.getContext().toUpperCase()).delimiter();

        // This is mostly for paragraphs which have no delimiter
        // and so they don't need a new line before or after the delimiter
        var joiner = (delimiter.isEmpty()) ? new StringJoiner("")
                                           : new StringJoiner("", delimiter + "\n", "\n" + delimiter);
        source.append(joiner.add(block.getSource()));
    }

//        Regex to look for the numbered callout in regular source and xml
//        var hasCallout = block.getSource().matches("<(!--)?(\\d+|\\.)(--)?>");
//        if ((hasCallout)) {
//            source.append("\n");
//        } else {
//            source.append("\n\n");
//        }

    private void extractList() {
        var list = (List) node;

        // It SHOULD be safe to assume that there's at least one entry
        var tempMarker = ((ListItem) list.getItems().get(0)).getMarker();

        // Do a little dance to get just the marker if they used numbers before the marker
        var marker = tempMarker.matches("\\d+\\.") ? tempMarker.replaceAll("\\d", "")
                                                         : tempMarker;
        var listItemJoiner = new StringJoiner("\n" + marker + " ", marker + " ", "");

        list.getItems().forEach(node -> {
            // Nested lists, should also work if a list has more than just a basic text for an item
            var item = (ListItem) node;
            if (item.getBlocks() != null && item.getBlocks().size() > 0) {
                // We need the item that has the nested list or block as well
                StringBuilder itemSource = new StringBuilder(item.getSource());

                // Get all the blocks or nested lists
                item.getBlocks().forEach(listItemBlock -> {
                    itemSource.append("\n");

                    // Need to attach the block to the list
                    // However, nested lists don't need the "+"
                    if (!((listItemBlock instanceof ListItem) || (listItemBlock instanceof List)))
                        itemSource.append("+\n");

                    itemSource.append(new SourceExtractor(listItemBlock).getSource());
                });
                // Now add list/blocks to the main list
                listItemJoiner.add(itemSource);
            } else { // Just a normal list item, nothing special
                listItemJoiner.add(item.getSource());
            }
        });

        // new line after the metadata
        if (source.length() > 0)
            source.append("\n");

        if (list.getTitle() != null)
            source.append(".").append(list.getTitle()).append("\n");

        source.append(listItemJoiner.toString());
    }

    private void extractTable() {
        var table = (Table) node;
        var delimiter = "|===";

        var bodyJoiner = new StringJoiner("\n| ", "\n| ", "");

        // We need to do a depth first join to make sure it lays out correctly
        // TODO: what about a nested table?
        table.getBody().forEach(row -> {
            var cellJoiner = new StringJoiner(" | ");
            row.getCells().forEach(cell -> {
                cellJoiner.add(cell.getSource());
            });
            bodyJoiner.merge(cellJoiner);
        });

        source
                // New line for after the metadata
                .append("\n")
                .append(delimiter)
                // Now the actual body
                .append(bodyJoiner)
                .append("\n")
                .append(delimiter);
    }

    private void extractDescriptionList() {
        var descriptionList = (DescriptionList) node;

        for (var listEntryIter = descriptionList.getItems().iterator(); listEntryIter.hasNext(); ) {
            var entry = listEntryIter.next();// each term
            // It is easier to use an iterator to know if we need a space or new line at the end
            for (var termIter = entry.getTerms().iterator(); termIter.hasNext(); ) {
                var term = termIter.next();

                if (term.getBlocks().isEmpty())
                    source.append(term.getSource());
                else
                    term.getBlocks().forEach(block -> source.append(new SourceExtractor(block).getSource()));

                if (termIter.hasNext())
                    source.append("::\n");
                else
                    source.append(":: ");
            }

            // description
            final var description = entry.getDescription();
            if (description.getBlocks().isEmpty()) {
                source.append(description.getSource());
            } else {
                description.getBlocks().forEach(block -> source.append(new SourceExtractor(block).getSource()));
            }

            if (listEntryIter.hasNext())
                source.append("\n");
        }
    }

    private boolean hasRoles() {
        return !node.getRoles().isEmpty();
    }

    private boolean hasAttributes() {
        return !node.getAttributes().isEmpty();
    }

    private boolean hasStyle() {
        return (node instanceof StructuralNode) && ((StructuralNode) node).getStyle() != null;
    }

    private boolean hasTitle() {
        // Section and List titles need to be handled differently
        // True if node is not a Section or List and has a non-null title
        return (node instanceof StructuralNode && !((node instanceof Section) || (node instanceof List)))
                && ((StructuralNode) node).getTitle() != null;
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
        var hasTitle = hasTitle();

        // If the node has any of these (id, roles, attributes, style, or title)
        // We need to handle it so it is above the node in the output
        if (hasId || hasRoles || hasAttributes || hasStyle || hasTitle) {

            // Titles could be an actual title (.Some title) above the node
            // or it could also be an attribute (though you rarely see that)
            if ((hasAttributes && node.getAttributes().containsKey("title")) || hasTitle) {
                if (hasTitle)
                    source.append(".").append(((StructuralNode) node).getTitle()).append("\n");
                else
                    source.append(".").append(node.getAttribute("title")).append("\n");
            }

            // Now we're into the id, role, style, attributes which will surrounded by [ and ]
            var mainJoiner = new StringJoiner(" ", "[", "]");

            if (hasId) {
                // If there isn't an explicit id for a section, it starts with an _
                if (id.startsWith("_")) {
                    // Don't use the first character (an underscore) and replace underscore with hyphen
                    id = id.substring(1).replaceAll("_", "-");
                }
                var idWithoutContext = id.split("_")[0];
                mainJoiner.add("id=\"" + idWithoutContext + "_{context}\"");
            }

            // Style, roles, and attributes are separated by a ',' and not a ' '
            var joiner = new StringJoiner(",", " ", "");

            if (hasStyle) {
                joiner.add(((StructuralNode) node).getStyle());

                // You typically see source and language together, so we'll put them together as well.
                if (node.hasAttribute("language"))
                    joiner.add(node.getAttribute("language").toString());
            }

            if (hasRoles)
                joiner.add("role=\"" + String.join(",", node.getRoles()) + "\"");

            if (hasAttributes) {
                // We have already added some of the attributes above, we don't need to add them twice.
                var keys = new java.util.HashSet<>(node.getAttributes().keySet());
                keys.remove("style");
                keys.remove("title");
                keys.remove("language");

                keys.stream()
                        // We don't really need the positional ones as they'll be listed as named (I think)
                        .filter(s -> s.matches("\\D+.*"))
                        .forEach(key -> joiner.add(key + "=\"" + node.getAttribute(key).toString() + "\""));
            }

            // Join everything together
            source.append(mainJoiner.merge(joiner).toString());
        }
    }
}
