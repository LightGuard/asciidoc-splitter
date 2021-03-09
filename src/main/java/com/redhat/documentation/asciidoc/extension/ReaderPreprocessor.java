package com.redhat.documentation.asciidoc.extension;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.PreprocessorReader;

/**
 * Preprocessor to get the lines of the source document.
 */
public class ReaderPreprocessor extends Preprocessor {
    public static final String SPLITTER_COMMENT = "// -- splitter comment -- ";
    private List<String> lines;
    private StringBuilder assemblyBody;

    @Override
    public void process(Document document, PreprocessorReader reader) {
        lines = reader.lines();
        reader.terminate();

        assemblyBody = new StringBuilder();
        boolean withinComment = false;
        boolean withinModule = false;
        boolean preprocessorWithinModule = false;
        var folderName = Path.of(document.getSourceLocation().getDir()).getFileName();

        // Regex used for finding a few things used in the loop
        var idPattern = Pattern.compile("\\[id=\"(?<moduleId>(con|ref|proc)-.+)_\\{context}\"]");
        var levelOffsetPattern = Pattern.compile("(?<offsetSize>^=+) .*");
        var preProcessStartPattern = Pattern.compile("if(n?)(def|eval)::(.+)?\\[(.+)?]$");

        // We need to look at each line to check for ifdefs, I wish there were a better way to do this.
        for (int i = 0; i < lines.size(); i++) {
            var currLine = lines.get(i);
            var idMatcher = idPattern.matcher(currLine);

            // Flip the comment section
            if (currLine.matches("^////")) {
                withinComment = !withinComment;
            }

            // No longer in a module
            if (currLine.trim().isEmpty()) {
                if (idPattern.matcher(lines.get(i + 1)).matches())
                    withinModule = false;

                // check to see if the next section is within a conditional
                if (preProcessStartPattern.matcher(lines.get(i + 1)).matches()) {
                    preprocessorWithinModule = false;

                    // We also need to guard against IndexOutOfBounds
                    if (i + 2 < lines.size() && idPattern.matcher(lines.get(i + 2)).matches())
                        withinModule = false;
                }
            }

            // If this is a module (by checking the id matches), create the appropriate include in the assembly body
            if (idMatcher.matches()) {
                withinModule = true;
                var levelOffsetMatcher = levelOffsetPattern.matcher(lines.get(i + 1));

                if (idMatcher.matches() && levelOffsetMatcher.matches() && !withinComment) {
                    assemblyBody.append("include::modules")
                            .append(File.separator)
                            .append(folderName)
                            .append(File.separator)
                            .append(idMatcher.group("moduleId")).append(".adoc")
                            .append("[leveloffset=+")
                            .append(levelOffsetMatcher.group("offsetSize").length() - 1)
                            .append("]").append("\n\n");
                }
            }

            // Special case for additional resources
            if ((currLine.contains("[role=\"_additional-resources\"]") &&
                 lines.get(i + 1).toLowerCase().contains("== additional resources")) && !withinComment) {
                withinModule = false;
                // Get the additional resources until a section break or the end of a preprocessor
                for (int j = 0; j < lines.size(); j++) {
                    final String nextLine = lines.get(i + j);
                    if (nextLine.startsWith("endif::") || idPattern.matcher(nextLine).matches() || i + j > lines.size()) {
                        break;
                    } else {
                        assemblyBody.append(nextLine).append("\n");
                    }
                }
            }

            // Preprocessor hell
            if (preProcessStartPattern.matcher(currLine).matches() || currLine.startsWith("endif::")) {
                // I want preprocessor directives ignored
                lines.set(i, SPLITTER_COMMENT + currLine);

                // special case endif (check for bounds, and also next and next next line for module boundary
                if (currLine.startsWith("endif::") && !withinModule && !preprocessorWithinModule
                    && (i + 1 < lines.size() && i + 2 < lines.size())
                    && (idPattern.matcher(lines.get(i + 1)).matches() || idPattern.matcher(lines.get(i + 2)).matches())) {
                    assemblyBody.append(currLine).append("\n");
                    continue;
                }

                // Case for additional resources within a preprocessor directive
                if (!currLine.trim().matches("if(n?)def::(.+)?\\[.+]$") &&
                    ((!withinComment && !withinModule) || lines.get(i + 1).contains("[role=\"_additional-resources\"]"))) {
                    preprocessorWithinModule = true;
                    assemblyBody.append(currLine).append("\n");
                }

                // Extra special case: ending a tag, ending a preconditional, starting a tag, starting a new module
                if (currLine.startsWith("endif::")) {
                    if (lines.get(i - 1).startsWith("// end::"))
                        if (i + 1 < lines.size() && lines.get(i + 1).trim().isEmpty())
                            if (i + 2 < lines.size() && lines.get(i + 2).startsWith("// tag::"))
                                if (i + 3 < lines.size() && idPattern.matcher(lines.get(i + 3)).matches()) {
                                    assemblyBody.append(currLine).append("\n");
                                }
                }
            }
        }

        reader.restoreLines(lines);
    }

    public List<String> getLines() {
        return Collections.unmodifiableList(lines);
    }

    public StringBuilder getAssemblyBody() {
        return assemblyBody;
    }

    public void updateLines(int start, int end, List<String> content) {
        var prevLines = lines.subList(start - 1, end); // New zero based

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
}
