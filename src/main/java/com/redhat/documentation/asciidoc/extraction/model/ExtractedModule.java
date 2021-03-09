package com.redhat.documentation.asciidoc.extraction.model;

import java.util.Objects;

import com.redhat.documentation.asciidoc.Util;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;

public class ExtractedModule {
    private String id;
    private Section section;
    private String source;
    private String moduleType;
    private int leveloffset = 0;
    private boolean additonalResources = false;
    private String folder;

    static boolean isNodeAModule(StructuralNode node) {
        if (node.getAttributes().containsKey(Util.MODULE_TYPE_ATTRIBUTE))
            return true;

        // Additional Resources (as a section) is considered a module, but should not be output as one.
        if (node.getAttributes().containsValue("_additional-resources"))
            return true;

        var nodeId = node.getId();

        if (nodeId != null) {
            if (nodeId.startsWith("chap-"))
                return false;

            if (nodeId.startsWith("proc-") || nodeId.startsWith("con-") || nodeId.startsWith("ref-"))
                return true;

            if (nodeId.endsWith("-proc") || nodeId.endsWith("-con") || nodeId.endsWith("-ref"))
                return true;
        }

        return false;
    }

    public String getFolder() {
        if (this.folder == null) {
            this.folder = this.section.getAttribute("splitter-doc-root", "", true).toString();
        }
        return folder;
    }

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

        this.moduleType = Util.getModuleType(section);

        this.section = section;
        this.leveloffset = section.getLevel();

        var newLines = new StringBuilder();
        for (String s : lines.split("\n")) {
            if (s.startsWith("=")) {
                newLines.append(s.replaceAll("^=.", "=".repeat(this.leveloffset - 1)));
            } else {
                newLines.append(s);
            }
            newLines.append("\n");
        }

        this.source = newLines.toString();
        this.additonalResources = "Additional resources".equalsIgnoreCase(section.getTitle());
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
        if (id.contains(moduleType))
            return id + ".adoc".toLowerCase();

        return moduleType + "-" + id + ".adoc".toLowerCase();
    }

    public String getModuleType() {
        return moduleType;
    }

    public int getLeveloffset() {
        return leveloffset;
    }

    public boolean isAdditonalResources() {
        return additonalResources;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtractedModule that = (ExtractedModule) o;
        return id.equals(that.id) && moduleType.equals(that.moduleType) && source.equals(that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, moduleType, source);
    }
}
