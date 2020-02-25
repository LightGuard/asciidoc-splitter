package com.redhat.documentation.asciidoc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.Section;

public class ExtractedModule {
    private String id;
    private Section section;
    private List<String> sources;

    public ExtractedModule(Section section) {
        // According to the modular docs, there should only be one underscore used to split the context.
        // We want the first part of that split
        this.id = section.getId().split("_")[0];
        this.section = section;
        this.sources = new ArrayList<>();

        section.getBlocks().stream()
                            .filter(ContentNode::isBlock)
                            .map(Block.class::cast)
                            .map(Block::getLines)
                            .forEach(this::addSource);
    }

    private void addSource(List<String> strings) {
        strings.stream().forEach(this::addSource);
    }

    public void addSource(String source) {
        this.sources.add(source);
    }

    public String getId() {
        return id;
    }

    public Section getSection() {
        return section;
    }

    public List<String> getSources() {
        return Collections.unmodifiableList(this.sources);
    }

    public String getFileName() {
        return id + ".adoc";
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
