package com.redhat.documentation.asciidoc;

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

    @Test
    public void testSourceListing() throws Exception {
        var adoc = """
                .app.rb
                [source,ruby,subs="attributes"]
                ----
                require 'sinatra'

                get '/hi' do
                  "Hello World!"
                end
                ----""";

        var document = asciidoctor.load(adoc, optionsBuilder.asMap());
        var blocks = document.getBlocks();

        assertThat(blocks).hasSize(1);
        var extractor = new SourceExtractor(blocks.get(0));

        assertThat(extractor.getSource()).isEqualTo(adoc);
    }

    @Test
    public void testBasicTable() throws Exception {
        var adoc = """
                |===
                | Row one, Cell one | Row one, Cell two
                | Row two, Cell one | Row two, Cell two
                |===""";

        var document = asciidoctor.load(adoc, optionsBuilder.asMap());
        var blocks = document.getBlocks();

        assertThat(blocks).hasSize(1);
        var extractor = new SourceExtractor(blocks.get(0));

        var expected = """
                [table,tablepcwidth="100",rowcount="2",colcount="2"]
                |===
                | Row one, Cell one | Row one, Cell two
                | Row two, Cell one | Row two, Cell two
                |===""";

        assertThat(extractor.getSource()).isEqualTo(expected);
    }

    // TODO: I need a more advanced table to test

    @Test
    public void testBasicOrderedList() throws Exception {
        var adoc = """
                . Protons
                . Electrons
                . Neutrons""";

        var document = asciidoctor.load(adoc, optionsBuilder.asMap());
        var blocks = document.getBlocks();

        assertThat(blocks).hasSize(1);
        var extractor = new SourceExtractor(blocks.get(0));

        var expected = """
                [arabic]
                . Protons
                . Electrons
                . Neutrons""";

        assertThat(extractor.getSource()).isEqualTo(expected);
    }

    @Test
    public void testBasicOrderedListWithNumbers() throws Exception {
        var adoc = """
                1. Protons
                2. Electrons
                3. Neutrons""";

        var document = asciidoctor.load(adoc, optionsBuilder.asMap());
        var blocks = document.getBlocks();

        assertThat(blocks).hasSize(1);
        var extractor = new SourceExtractor(blocks.get(0));

        var expected = """
                [arabic]
                . Protons
                . Electrons
                . Neutrons""";

        assertThat(extractor.getSource()).isEqualTo(expected);
    }

    @Test
    public void testAdvancedOrderedList() throws Exception {
        var adoc = """
                [%reversed,start=3]
                .Title
                . Protons
                . Electrons
                . Neutrons""";

        var document = asciidoctor.load(adoc, optionsBuilder.asMap());
        var blocks = document.getBlocks();

        assertThat(blocks).hasSize(1);
        var extractor = new SourceExtractor(blocks.get(0));

        var expected = """
                [arabic,start="3",reversed-option=""]
                .Title
                . Protons
                . Electrons
                . Neutrons""";

        assertThat(extractor.getSource()).isEqualTo(expected);
    }

    @Test
    public void testComplexListContent() throws Exception {
        var adoc = """
                * The header in AsciiDoc must start with a document title.
                +
                ----
                = Document Title
                ----
                +
                Keep in mind that the header is optional.

                * Optional Author and Revision information immediately follows the header title.
                +
                ----
                = Document Title
                Doc Writer <doc.writer@asciidoc.org>
                v1.0, 2013-01-01
                ----""";

        var document = asciidoctor.load(adoc, optionsBuilder.asMap());
        var blocks = document.getBlocks();

        assertThat(blocks).hasSize(1);
        var extractor = new SourceExtractor(blocks.get(0));

        var expected = """
                * The header in AsciiDoc must start with a document title.
                +
                [listing]
                ----
                = Document Title
                ----
                +
                Keep in mind that the header is optional.
                * Optional Author and Revision information immediately follows the header title.
                +
                [listing]
                ----
                = Document Title
                Doc Writer <doc.writer@asciidoc.org>
                v1.0, 2013-01-01
                ----""";

        assertThat(extractor.getSource()).isEqualTo(expected);
    }

    @Test
    public void testNestedOrderedList() throws Exception {
        var adoc = """
                . Protons
                .. Electrons
                . Neutrons""";

        var document = asciidoctor.load(adoc, optionsBuilder.asMap());
        var blocks = document.getBlocks();

        assertThat(blocks).hasSize(1);
        var extractor = new SourceExtractor(blocks.get(0));

        var expected = """
                [arabic]
                . Protons
                [loweralpha]
                .. Electrons
                . Neutrons""";

        assertThat(extractor.getSource()).isEqualTo(expected);
    }

    @Test
    public void testBasicUnorderedList() throws Exception {
        var adoc = """
                * Protons
                * Electrons
                * Neutrons""";

        var document = asciidoctor.load(adoc, optionsBuilder.asMap());
        var blocks = document.getBlocks();

        assertThat(blocks).hasSize(1);
        var extractor = new SourceExtractor(blocks.get(0));

        var expected = """
                * Protons
                * Electrons
                * Neutrons""";

        assertThat(extractor.getSource()).isEqualTo(expected);
    }

    @Test
    public void testDescriptionList() {
        var adoc = """
                CPU:: The brain of the computer.
                Hard drive:: Permanent storage for operating system and/or user files.
                RAM:: Temporarily stores information the CPU uses during operation.
                Keyboard:: Used to enter text or control items on the screen.
                Mouse:: Used to point to and select items on your computer screen.
                Monitor:: Displays information in visual form using text and graphics.""";


        var document = asciidoctor.load(adoc, optionsBuilder.asMap());
        var blocks = document.getBlocks();

        assertThat(blocks).hasSize(1);
        var extractor = new SourceExtractor(blocks.get(0));

        assertThat(extractor.getSource()).isEqualTo(adoc);
    }
}
