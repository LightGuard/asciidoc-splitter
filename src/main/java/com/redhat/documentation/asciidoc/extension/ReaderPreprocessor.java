package com.redhat.documentation.asciidoc.extension;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.regex.Pattern;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.PreprocessorReader;

/**
 * Preprocessor to get the lines of the source document.
 */
public class ReaderPreprocessor extends Preprocessor {
    private List<String> lines;

    @Override
    public void process(Document document, PreprocessorReader reader) {
//        lines = reader.readLines();
        lines = reader.lines();
        reader.terminate();

        boolean containsIfEval = false;
        var ifDefPattern = Pattern.compile("ifdef::(?<gate>.*)\\[]");
        Stack<DirectiveSection> directiveSections = new Stack<>();
        Stack<Integer> startLines = new Stack<>();
        Stack<Integer> endLines = new Stack<>();
        Stack<String> gates = new Stack<>();

        // We need to look at each line to check for ifdefs, I wish there were a better way to do this.
        for (int i = 0; i < lines.size(); i++) {
            var currLine = lines.get(i);
            var ifDefMatcher = ifDefPattern.matcher(currLine);

            if (currLine.contains("ifeval::")) {
                containsIfEval = true;
            }

            if (ifDefMatcher.matches()) { // We don't need single line
                startLines.push(i);
                gates.push(ifDefMatcher.group("gate").toLowerCase());
            }

            if (currLine.contains("endif::")) {
                if (containsIfEval) {
                    // Skip if we have an ifeval and reset ifeval check
                    containsIfEval = false;
                    continue;
                }

                endLines.push(i);

                if (endLines.size() > startLines.size()) {
                    throw new RuntimeException("Unbalanced endif at line " + i);
                }

                // If we have the attribute so the gate resolves correctly, only remove the ifdef and endif
                // otherwise, remove the whole section
                if (document.getAttributes().containsKey(gates.pop())) {
                    directiveSections.push(new DirectiveSection(startLines.pop(), endLines.pop(), false));
                } else {
                    directiveSections.push(new DirectiveSection(startLines.pop(), endLines.pop(), true));
                }
            }
        }

        while (!directiveSections.empty()) {
            // TODO: Check to see if the current one is nested within the next one
            // If yes and removeSection true
            //   continue and do the next one
            // If yes and removeSection false
            //   replace with
            // If no
            //   continue as normal
            var section = directiveSections.pop();

            if (section.shouldRemoveSection()) {
                Collections.fill(lines.subList(section.getStart(), section.getEnd() + 1), "");
            } else {
                lines.set(section.getEnd(), "");
                lines.set(section.getStart(), "");
            }
        }

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
