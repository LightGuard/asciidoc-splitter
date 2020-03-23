package com.redhat.documentation.asciidoc.processor;

import java.util.Map;

import com.redhat.documentation.asciidoc.extraction.AsciidocExtractionTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class AttributeTreeProcessorTest extends AsciidocExtractionTest {
    @Test
    @Disabled("Mostly used for a trail of attributes")
    public void findAttributesTest() throws Exception {
        asciidoctor.javaExtensionRegistry().treeprocessor(AttributeTreeProcessor.class);

        var adoc = "= Tests `testing`\n" +
                   ":my-attribute: Hello\n" +
                   "\n" +
                   "[rewrite=\"all of it\"]\n" +
                   "== New Section with `.jsh`\n" +
                   ":my-attribute: Hola\n" +
                   "\n" +
                   "[my-attribute=\"Good-bye\"]\n" +
                   "{my-attribute} World!\n";

        var doc = asciidoctor.load(adoc, optionsBuilder.asMap());
        var para = doc.findBy(Map.of("context", ":paragraph"));
        para.get(0);
    }
}

