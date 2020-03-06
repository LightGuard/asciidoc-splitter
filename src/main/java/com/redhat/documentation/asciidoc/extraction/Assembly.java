package com.redhat.documentation.asciidoc.extraction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.asciidoctor.ast.Section;

public class Assembly {
    private String id;
    private String idWithoutContext;
    private String context;
    private List<ExtractedModule> modules;
    private StringBuilder source;

    public Assembly(Section section) {
        this.id = section.getId();
        // If there isn't an explicit id, it starts with an _
        if (this.id.startsWith("_")) {
            // Don't use the first character (an underscore) and replace underscore with hyphen
            this.id = this.id.substring(1).replaceAll("_", "-");
        }
        this.idWithoutContext = this.id.split("_")[0];
        this.context = this.id;
        this.modules = new ArrayList<>();
        this.source = new StringBuilder();

        // TODO Figure out where/how to refactor this to use the SourceExtractor

        // Adding the id of the module
        this.source.append("[id=\"").append(this.idWithoutContext).append("_{context}\"]\n")
                    // Adding the section title
                   .append("= ").append(section.getTitle()).append("\n")
                   .append(":context: ").append(this.idWithoutContext).append("\n\n");

        section.getBlocks().forEach(sectionBlock -> {
            // This is a module
            if (sectionBlock instanceof Section && sectionBlock.getLevel() == 2) {
                final var extractedModule = new ExtractedModule((Section) sectionBlock);
                this.modules.add(extractedModule);
                this.source.append("include::modules/")
                           .append(extractedModule.getFileName())
                           .append("[leveloffset=+1]")
                           .append("\n\n");
            } else {
                // Add it to the actual source
                this.source.append(new SourceExtractor(sectionBlock).getSource())
                        .append("\n\n");
            }
        });
    }

    public void addModule(ExtractedModule module) {
        this.modules.add(module);
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
        return idWithoutContext + ".adoc";
    }
}
