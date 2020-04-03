package com.redhat.documentation.asciidoc.extraction;

import org.asciidoctor.ast.Section;

public class SectionWrapper {
    private Section section;
    private String source;

    public SectionWrapper(Section section, String source) {
        this.section = section;
        this.source = source;
    }

    public Section getSection() {
        return section;
    }

    public String getSource() {
        return source;
    }
}
