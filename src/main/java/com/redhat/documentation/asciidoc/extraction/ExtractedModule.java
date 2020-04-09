package com.redhat.documentation.asciidoc.extraction;

import java.util.Objects;

import com.redhat.documentation.asciidoc.Util;
import org.asciidoctor.ast.Section;

public class ExtractedModule {
    private String id;
    private Section section;
    private String source;
    private String moduleType;

    @Override
    public String toString() {
        return "ExtractedModule{" +
                "id='" + id + "_{context}'" +
                //", section=" + section +
                // ", sources=" + sources +
                ", parentid=" + Util.getFullId(section.getParent()) +
                '}';
    }

    public ExtractedModule(Section section, String lines) {
        // According to the modular docs, there should only be one underscore used to split the context.
        // We want the first part of that split
        // If there isn't an explicit id, it starts with an _
        if (section.getId().startsWith("_")) {
            // Don't use the first character (an underscore) and replace underscore with hyphen
            this.id = section.getId().substring(1).replaceAll("_", "-");
        } else {
            this.id = section.getId().split("_")[0];
        }

        // There could already be a "{context}" in the id, we don't need it
        if (this.id.contains("{context}")) {
            this.id = this.id.substring(0, this.id.lastIndexOf("{context}") - 1);
        }

        if (section.getAttributes().containsKey(Assembly.MODULE_TYPE_ATTRIBUTE)) {
            this.moduleType = section.getAttributes().get(Assembly.MODULE_TYPE_ATTRIBUTE).toString();
        } else {
            this.moduleType = "unknown";
        }

        this.section = section;
        this.source = lines;
    }

    public String getId() {
        return id;
    }

    public Section getSection() {
        return section;
    }

    public String getSource() {
        return this.source;
    }

    public String getFileName() {
        return moduleType + "-" + id + ".adoc";
    }

    public String getModuleType() {
        return moduleType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtractedModule that = (ExtractedModule) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
