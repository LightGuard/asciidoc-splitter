package com.redhat.documentation.asciidoc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Section;

public class Assembly {
    private String id;
    private String idWithoutContext;
    private String context;
    private List<ExtractedModule> modules;
    private StringBuilder source;

    public Assembly(Section section) {
        this.id = section.getId();
        this.idWithoutContext = this.id.split("_")[0];
        this.context = section.getId();
        this.modules = new ArrayList<>();
        this.source = new StringBuilder();

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
            }

            // Add it to the actual source
            if (sectionBlock instanceof Block) {
                final var block = (Block) sectionBlock;
                this.source.append(block.getSource())
                           .append("\n\n");
                // TODO: Probably need to do something about metadata here if it exists
            }

            // TODO: Table
            // TODO: List
            // TODO: DescriptionList
            // TODO: Any others?
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
