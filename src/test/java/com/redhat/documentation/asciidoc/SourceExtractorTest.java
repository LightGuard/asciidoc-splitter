package com.redhat.documentation.asciidoc;

import java.io.File;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SourceExtractorTest extends AsciidocExtractionTest {

    @Test
    public void testSectionExtraction() throws Exception {
        var sectionAdoc = """
                == A new section
                """;
        var document = asciidoctor.load(sectionAdoc, optionsBuilder.asMap());
        var blocks = document.getBlocks();

        assertThat(blocks).isNotEmpty();
        blocks.forEach(structuralNode -> {
            var extractor = new SourceExtractor(structuralNode);
            assertThat(extractor.getSource())
                    .isEqualTo("""
                    [id="a-new-section_{context}"]
                    = A new section
                    :context: a-new-section""");
        });
    }

    @Test
    public void testBasicParagraph() throws Exception {
        var adoc = """
                Some basic text in a Paragraph.
                With two lines.""";

        var document = asciidoctor.load(adoc, optionsBuilder.asMap());
        var blocks = document.getBlocks();

        assertThat(blocks).hasSize(1);
        var extractor = new SourceExtractor(blocks.get(0));

        assertThat(extractor.getSource()).isEqualTo(adoc);
    }

    @Test
    public void testMoreAdvancedParagraph() throws Exception {
        var adoc = """
                Some basic text in a Paragraph.
                Here's some `text` with inline markup.
                And a https://google.com[link].
                Oh, and maybe an image::404.png[]""";

        var document = asciidoctor.load(adoc, optionsBuilder.asMap());
        var blocks = document.getBlocks();

        assertThat(blocks).hasSize(1);
        var extractor = new SourceExtractor(blocks.get(0));

        assertThat(extractor.getSource()).isEqualTo(adoc);
    }
}
