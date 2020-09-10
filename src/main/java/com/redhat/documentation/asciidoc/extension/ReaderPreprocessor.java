package com.redhat.documentation.asciidoc.extension;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.PreprocessorReader;

/**
 * Preprocessor to get the lines of the source document.
 */
public class ReaderPreprocessor extends Preprocessor {
    public static final String SPLITTER_COMMENT = "// -- splitter comment -- ";
    private List<String> lines;

    @Override
    public void process(Document document, PreprocessorReader reader) {
        lines = reader.lines();
        reader.terminate();

        boolean containsIfEval = false;
        Stack<DirectiveSection> directiveSections = new Stack<>();
        Stack<Integer> startLines = new Stack<>();
        Stack<Integer> endLines = new Stack<>();
        Stack<String> gates = new Stack<>();

        // We need to look at each line to check for ifdefs, I wish there were a better way to do this.
        for (int i = 0; i < lines.size(); i++) {
            var currLine = lines.get(i);

            if (currLine.startsWith("ifdef::") || currLine.startsWith("ifndef::") ||
                currLine.startsWith("ifeval::") || currLine.startsWith("endif::")) {
                lines.set(i, SPLITTER_COMMENT + currLine);
            }
        }

            // xref stuff
//            var filename = document.getSourceLocation().getFile();
//            var path = Path.of(document.getSourceLocation().getDir()).getFileName();
//            if (currLine.contains("xref:")) {
//                lines.set(i, currLine.replaceAll("xref:(?<ref>.+)\\[(?<attribs>.*)]",
//                                        "include::" + path + "/" + filename + "[tags=${ref}]"));
//            }
//
//            if (currLine.contains("<<")) {
//                lines.set(i, currLine.replaceAll("<<(?<ref>.+),?(?<attribs>.*)>>",
//                                        "include::" + path + "/"  + filename + "[tags=${ref}]"));
//            }

        reader.restoreLines(lines);
    }

    public List<String> getLines() {
        return Collections.unmodifiableList(lines);
    }

    public void updateLines(int start, int end, List<String> content) {
        var prevLines = lines.subList(start -1 , end); // New zero based

        if (prevLines.size() < content.size()) {
            throw new IllegalStateException("Adding more content than replacing with \"replace-with\" starting at line "
                                            + start + ". This will throw off line numbers for further processing.");
        }
        // Clear out any existing content
        Collections.fill(prevLines, "");

        // Remove the existing value at the specified index and add back in the new content.
        for (int i = 0; i < content.size(); i++) {
            prevLines.set(i, content.get(i));
        }
    }

    class DirectiveSection {
        private final int start;
        private final int end;
        private final boolean removeSection;

        DirectiveSection(int start, int end, boolean removeSection) {
            this.start = start;
            this.end = end;
            this.removeSection = removeSection;
        }

        int getStart() {
            return start;
        }

        int getEnd() {
            return end;
        }

        boolean shouldRemoveSection() {
            return removeSection;
        }

        @Override
        public String toString() {
            return "DirectiveSection{" +
                   "start=" + start +
                   ", end=" + end +
                   ", removeSection=" + removeSection +
                   '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DirectiveSection that = (DirectiveSection) o;
            return start == that.start &&
                   end == that.end &&
                   removeSection == that.removeSection;
        }

        @Override
        public int hashCode() {
            return Objects.hash(start, end, removeSection);
        }
    }
}
