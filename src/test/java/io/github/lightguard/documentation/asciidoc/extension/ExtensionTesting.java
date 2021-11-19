package io.github.lightguard.documentation.asciidoc.extension;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Map;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.extension.JavaExtensionRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ExtensionTesting {

    private Asciidoctor asciidoctor;
    private JavaExtensionRegistry registry;
    private OptionsBuilder optionsBuilder;
    private Map<String, Object> processorConfig;

    @BeforeEach
    private void setup() {
        optionsBuilder = OptionsBuilder.options();
        asciidoctor = Asciidoctor.Factory.create();
        registry = asciidoctor.javaExtensionRegistry();


        // We need access to the line numbers and source
        optionsBuilder.sourcemap(true);
    }

    @Test
    public void testTreeProcessorBlockChange() throws URISyntaxException {
        var readerPreprocessor = new ReaderPreprocessor();
        var treeprocessor = new ReplaceWithTreeProcessor();

        treeprocessor.setReaderPreprocessor(readerPreprocessor);

        registry.preprocessor(readerPreprocessor)
                .treeprocessor(treeprocessor);

        var adoc = new File(this.getClass().getClassLoader().getResource("docs/processor-test/replacewith.adoc").toURI());
        var doc = asciidoctor.loadFile(adoc, optionsBuilder.asMap());
        var lines = readerPreprocessor.getLines();

        assertThat(lines).contains("include::new-doc.adoc[leveloffset=+1]");
        assertThat(lines).doesNotContain("[replace-with=\"new-doc.adoc\" replace-with-param=\"leveloffset=+1\"]");
    }
}
