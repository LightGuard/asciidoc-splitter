package io.github.lightguard.documentation.asciidoc.extension;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.PreprocessorReader;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

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
        boolean beforeAllModules = true;
        boolean preProcessorStartModule = false;
        var folderName = Path.of(document.getSourceLocation().getDir()).getFileName();

        // Regex used for finding a few things used in the loop
        var idPattern = Pattern.compile("\\[id=[\"'](?<moduleId>((con|ref|proc)-.+)|.+-(con|ref|proc))_\\{context}[\"']]");
        var levelOffsetPattern = Pattern.compile("(?<offsetSize>^=+) .*");
        var preProcessStartPattern = Pattern.compile("if(n?)(def|eval)::(.+)?\\[(.+)?]$");
        var contextAttribPattern = Pattern.compile("^:context:.*$");

        // We need to look at each line to check for ifdefs, I wish there were a better way to do this.
        for (int i = 0; i < lines.size(); i++) {
            var currLine = lines.get(i);
            var idMatcher = idPattern.matcher(currLine);

            // "strip out" attributes for this
            if (contextAttribPattern.matcher(currLine).matches()) {
                lines.set(i, SPLITTER_COMMENT + currLine);
            }

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
                    // We also need to guard against IndexOutOfBounds
                    if (i + 2 < lines.size() && idPattern.matcher(lines.get(i + 2)).matches()) {
                        preProcessorStartModule = true;
                        withinModule = false;
                    }
                }

                if (lines.get(i - 1).startsWith("// end::") && i + 1 < lines.size() && lines.get(i + 1).startsWith("// tag::")) {
                    assemblyBody.append(lines.get(i - 1).trim()).append("\n");
                }
            }

            // If this is a module (by checking the id matches), create the appropriate include in the assembly body
            if (idMatcher.matches()) {
                withinModule = true;
                beforeAllModules = false;
                var levelOffsetMatcher = levelOffsetPattern.matcher(lines.get(i + 1));

                // Add in the starting of a tag, if it exists
                if (i - 1 > 0 && lines.get(i - 1).startsWith("// tag::")) {
                    assemblyBody.append(lines.get(i - 1).trim()).append("\n");
                }

                if (levelOffsetMatcher.matches() && !withinComment) {
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

            // I want to check for starting and ending of tags on the line after we've seen it, to
            // verify if we're actually within a module or not.

            // end tag
            if (!withinModule && i - 1 > 0 && lines.get(i - 1).startsWith("// end::")) {
                assemblyBody.append(lines.get(i - 1).trim()).append("\n");
            }

            // Special case for additional resources or conclusion (kafka)
            if (currLine.contains("[#conclusion]") || (currLine.contains("[role=\"_additional-resources\"]") &&
                    lines.get(i + 1).toLowerCase().contains("== additional resources")) && !withinComment) {
                withinModule = false;
                // Get the additional resources until a section break or the end of a preprocessor
                for (int j = 0; i + j < lines.size(); j++) {
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
                if (currLine.startsWith("endif::")
                        && preProcessorStartModule
                        && ((i + 1 < lines.size() && idPattern.matcher(lines.get(i + 1)).matches())
                        || (i + 2 < lines.size() && idPattern.matcher(lines.get(i + 2)).matches()))) {
                    assemblyBody.append(currLine).append("\n");
                    preProcessorStartModule = false;
                }

                // Check for the end of a file
                if (currLine.startsWith("endif::")
                        && (i + 1 < lines.size() && lines.get(i + 1).trim().isEmpty())
                        && (i + 2 < lines.size() && preProcessStartPattern.matcher(lines.get(i + 2)).matches())
                        && lines.get(i + 2).contains("{parent-context}")) {
                    assemblyBody.append(currLine).append("\n");
                }

                // Also check if we're ending a tag and a preprocessor
                if (currLine.startsWith("endif::") && lines.get(i - 1).startsWith("// end::")) {
                    assemblyBody.append(lines.get(i - 1)).append("\n").append(currLine).append("\n");
                }

                // Case for an ifdef and a module starting or ending additional resources or conclusion (for kafka)
                if (preProcessStartPattern.matcher(currLine).matches()
                        && i + 1 < lines.size()
                        && (idPattern.matcher(lines.get(i + 1)).matches() || lines.get(i + 1).contains("[role=\"_additional-resources\"]") || lines.get(i + 1).contains("[#conclusion]"))) {
                    assemblyBody.append(currLine).append("\n");
                }

                // Starting a tag after a preprocessor
                if (preProcessStartPattern.matcher(currLine).matches()
                        && i + 1 < lines.size() && lines.get(i + 1).startsWith("// tag::")) {
                    assemblyBody.append(currLine).append("\n");
                }

                // Get the last two lines of the assembly file
                if (currLine.contains("parent-context") && !beforeAllModules) {
                    assemblyBody.append(currLine).append("\n");
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
