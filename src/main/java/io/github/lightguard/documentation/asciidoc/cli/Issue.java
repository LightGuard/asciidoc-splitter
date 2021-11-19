package io.github.lightguard.documentation.asciidoc.cli;

import org.asciidoctor.ast.StructuralNode;

/**
 * Represents an issue (warning/error) found in the processing of the documents.
 */
public class Issue {

    private final StructuralNode node;
    private final boolean error;
    private final String text;

    public static Issue error(String text, StructuralNode node) {
        return new Issue(true, text, node);
    }

    public static Issue nonerror(String text, StructuralNode node) {
        return new Issue(false, text, node);
    }

    Issue(boolean error, String text, StructuralNode node) {
        this.error = error;
        this.text = text;
        this.node = node;
    }


    public boolean isError() {
        return error;
    }

    @Override
    public String toString() {
        String contextinfo = null;
        if (node != null) {
            if (node.getSourceLocation() == null) {
                contextinfo = node.getClass().toString();
            } else {
                contextinfo = node.getSourceLocation().toString();
            }
        }

        return (error ? "ERROR: " : "WARNING: ") + contextinfo + ": " + text;
    }
}
