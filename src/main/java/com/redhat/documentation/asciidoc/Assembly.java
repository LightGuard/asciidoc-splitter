package com.redhat.documentation.asciidoc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.asciidoctor.ast.Section;

public class Assembly {
    private String id;
    private String context;
    private List<ExtractedModule> modules;

    public Assembly(Section section) {
        this.id = section.getId();
        this.context = section.getId();
        this.modules = new ArrayList<>();

        section.getBlocks().forEach(sectionBlock -> {
            if (sectionBlock instanceof Section && sectionBlock.getLevel() == 2)
                this.modules.add((new ExtractedModule((Section) sectionBlock)));
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
}
