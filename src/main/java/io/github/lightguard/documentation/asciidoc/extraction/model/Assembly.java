package io.github.lightguard.documentation.asciidoc.extraction.model;

import io.github.lightguard.documentation.asciidoc.Util;
import io.github.lightguard.documentation.asciidoc.extension.ReaderPreprocessor;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * An Assembly is essentially a full document from an asciidoc perspective.
 * An Assembly can contain links to modules and also have some additional text, typically before the includes, sort of like a preamble.
 */
public class Assembly {

    private String id;
    private String idWithoutContext;
    private String context;
    private List<ExtractedModule> modules;
    private StringBuilder source;
    private boolean createAssembly;

    public Assembly(Document doc, List<String> lines, StringBuilder processedBody) {
        this.id = (doc.getId() == null ? doc.getBlocks().get(0).getId() : doc.getId());
        // If there isn't an explicit id, it starts with an _
        if (this.id.startsWith("_")) {
            // Don't use the first character (an underscore) and replace underscore with hyphen
            this.id = this.id.substring(1).replaceAll("_", "-");
        }

        // remove "chap-" if it is found
        if (this.id.contains("chap-")) {
            this.id = this.id.replaceAll("chap-", "");
        }

        // remove "assembly-" if it is found
        if (this.id.contains("assembly-")) {
            this.id = this.id.replaceAll("assembly-", "");
        }

        if (this.id.contains("{context}")) {
            this.idWithoutContext = this.id.substring(0, this.id.lastIndexOf("{context}") - 1);
        } else {
            this.idWithoutContext = this.id;
        }

        this.context = this.id;
        this.modules = new ArrayList<>();
        this.source = new StringBuilder();

        this.createAssembly = Boolean.parseBoolean(doc.getAttribute("assembly", "true").toString());

        // Adding the id of the module
        this.source.append("[id='assembly-").append(this.idWithoutContext).append("']\n"); // I don't think we need the context for assemblies

        // Grab the preamble
        var sections = doc.findBy(Map.of("context", ":section"));

        // The first block should be the section with the document title
        final int preambleEndLineNumber = getPreambleEndLineNumber(doc, lines);

        for (int i = sections.get(0).getSourceLocation().getLineNumber() - 1; i < preambleEndLineNumber; i++) {
            this.source.append(Util.fixIncludes(lines.get(i))).append("\n");
        }

        List<SectionWrapper> moduleSources = new ArrayList<>();

        var modules = sections.stream()
                .filter(ExtractedModule::isNodeAModule)
                .map(Section.class::cast)
                .collect(Collectors.toList());

        var modulesItr = modules.listIterator();
        while (modulesItr.hasNext()) {
            var section = modulesItr.next();

            if (modulesItr.hasNext()) {
                // For sanity, Additional Resources is a module, but not counted as one here
                if (section.getAttributes().containsValue("_additional-resources"))
                    continue;

                var nextSection = modules.get(modulesItr.nextIndex());
                var sectionEndLineNumber = nextSection.getSourceLocation().getLineNumber() - 1;

                var nextSectionLine = lines.get(sectionEndLineNumber);
                // We have to find the end of this section by looking at the next section and going back looking for
                // a blank or empty string

                while (!(nextSectionLine.isEmpty() || nextSectionLine.isBlank()) || nextSectionLine.startsWith("endif::[]")) {
                    sectionEndLineNumber -= 1;
                    nextSectionLine = lines.get(sectionEndLineNumber);
                }

                // Add it to the list
                moduleSources.add(new SectionWrapper(section, getSectionSource(lines, section, sectionEndLineNumber)));
            } else {
                // For sanity, Additional Resources is a module, but not counted as one here
                if (section.getAttributes().containsValue("_additional-resources"))
                    continue;

                // Add it to the list
                moduleSources.add(new SectionWrapper(section, getSectionSource(lines, section, lines.size())));
            }
        }

        moduleSources.forEach(wrapper -> {
            var extractedModule = new ExtractedModule(wrapper.getSection(), wrapper.getSource());
            this.modules.add(extractedModule);
        });
        this.source.append("\n").append(processedBody);
    }

    private int getPreambleEndLineNumber(StructuralNode doc, List<String> lines) {
        // Find the first module, it may not be the first section
        var nextSection = doc.findBy(Map.of("context", ":section")).stream().filter(ExtractedModule::isNodeAModule).findFirst().orElseThrow();
        var sectionEndLineNumber = nextSection.getSourceLocation().getLineNumber() - 1;

        var nextSectionLine = lines.get(sectionEndLineNumber);
        // We have to find the end of this section by looking at the next section and going back looking for
        // a blank or empty string
        while (!(nextSectionLine.isEmpty() || nextSectionLine.isBlank())) {
            sectionEndLineNumber -= 1;
            nextSectionLine = lines.get(sectionEndLineNumber);
        }
        return sectionEndLineNumber;
    }

    private String getSectionSource(List<String> lines, StructuralNode section, int nextSectionStart) {
        var startingLine = section.getSourceLocation().getLineNumber();
        var unmatchedIfdef = false;
        var preProcessStartPattern = Pattern.compile(".*if(n?)(def|eval)::(.+)?\\[]$");
        StringBuilder sectionSource = new StringBuilder();
        for (int i = startingLine; i < nextSectionStart; i++) {
            if (lines.get(i).startsWith(ReaderPreprocessor.SPLITTER_COMMENT)
                && preProcessStartPattern.matcher(lines.get(i)).matches())
                unmatchedIfdef = true;

            if (i + 1 < nextSectionStart && lines.get(i).contains("endif::")) {
                unmatchedIfdef = false;
            }

//            // We don't want lines that contain with ifdef::
//            if (lines.get(i).contains("ifdef::"))
//                continue;
//
//            // We also don't want lines that contain endif::
//            if (lines.get(i).contains("endif::"))
//                continue;

            // We don't want to end with an endif
            if (i + 1 >= nextSectionStart && i + 1 < lines.size()
                && lines.get(i).startsWith(ReaderPreprocessor.SPLITTER_COMMENT + "endif::")
                && lines.get(i + 1).trim().isBlank()
                && !unmatchedIfdef) {
                unmatchedIfdef = false;
                continue;
            }

            sectionSource.append(Util.fixSectionLevelForModule(Util.tweakSource(lines.get(i)), section.getLevel())).append("\n");

        }
        return sectionSource.toString();
    }

    public String getId() {
        return id;
    }

    public String getContext() {
        return context;
    }

    public List<ExtractedModule> getModules() {
        return Collections.unmodifiableList(this.modules);
    }

    public String getSource() {
        return source.toString();
    }

    public String getFilename() {
        return "assembly-" + idWithoutContext + ".adoc".toLowerCase();
    }

    public boolean shouldCreateAssembly() {
        return this.createAssembly;
    }

    @Override
    public String toString() {
        return "Assembly{" +
               "id='" + id + '\'' +
               ", module size=" + modules.size() +
               ", createAssembly=" + createAssembly +
               '}';
    }
}

