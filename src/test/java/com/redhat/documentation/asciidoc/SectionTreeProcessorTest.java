package com.redhat.documentation.asciidoc;

import java.io.File;
import java.util.stream.Collectors;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SectionTreeProcessorTest extends AsciidocExtractionTest {
    private SectionTreeProcessor cut;
    private File sample;

    @BeforeEach
    void setUp() throws Exception {
        cut = new SectionTreeProcessor();
        sample = new File(SectionTreeProcessor.class.getClassLoader().getResource("docs/basic/sample.adoc").toURI());

        // Have to register our instance of the extension so we can pull information from it
        asciidoctor.javaExtensionRegistry().treeprocessor(cut);
    }

    @Test
    void testGetAssemblies() throws Exception {
        asciidoctor.loadFile(sample, optionsBuilder.asMap());

        assertThat(cut.getAssemblies()).hasSize(2);

        final var firstAssembly = cut.getAssemblies().get(0);
        assertThat(firstAssembly.getId()).isEqualTo("assembly-1_my-project");

        final var secondAssembly = cut.getAssemblies().get(1);
        assertThat(secondAssembly.getId()).isEqualTo("assembly-2_my-project");
    }

    @Test
    void testAssemblyStructure() throws Exception {
        asciidoctor.loadFile(sample, optionsBuilder.asMap());

        final var firstAssembly = cut.getAssemblies().get(0);
        assertThat(firstAssembly.getModules()).hasSize(3);
        assertThat(firstAssembly.getModules().stream().map(ExtractedModule::getId).collect(Collectors.toList()))
                .containsOnly("module-a", "module-b", "module-c");

        final var secondAssembly = cut.getAssemblies().get(1);
        assertThat(secondAssembly.getModules()).hasSize(3);
        assertThat(secondAssembly.getModules().stream().map(ExtractedModule::getId).collect(Collectors.toList()))
                .containsOnly("module-a", "module-b", "module-d");
    }

    @Test
    void testGetModules() throws Exception {
        asciidoctor.loadFile(sample, optionsBuilder.asMap());

        assertThat(cut.getModules()).hasSize(4);
    }
}
