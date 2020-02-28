package com.redhat.documentation.asciidoc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;
import org.asciidoctor.extension.Treeprocessor;

public class SectionTreeProcessor extends Treeprocessor {
    private List<Assembly> assemblies;
    private Set<ExtractedModule> modules;

    @Override
    public Document process(Document document) {
        this.assemblies = new ArrayList<>();
        this.modules = new HashSet<>();

        findSections(document);

        return document;
    }

    private void findSections(Document doc) {
        // TODO: What should we do with preamble?
        doc.getBlocks().forEach(node -> {
            if (node instanceof Section && node.getLevel() == 1) {
                var assembly = new Assembly((Section) node);
                this.assemblies.add(assembly);
                this.modules.addAll(assembly.getModules());
            }
        });
        // We should have all the assemblies, and all of their modules now
    }

    List<Assembly> getAssemblies() {
        return this.assemblies;
    }

    public List<ExtractedModule> getModules() {
        return List.copyOf(modules);
    }
}
