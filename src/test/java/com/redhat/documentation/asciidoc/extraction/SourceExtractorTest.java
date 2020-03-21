package com.redhat.documentation.asciidoc.extraction;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SourceExtractorTest extends AsciidocExtractionTest {

    @Test
    public void testSectionExtraction() throws Exception {
        var sectionAdoc = "== A new section\n";
        var document = asciidoctor.load(sectionAdoc, optionsBuilder.asMap());
        var blocks = document.getBlocks();

        assertThat(blocks).isNotEmpty();
        blocks.forEach(structuralNode -> {
            var extractor = new SourceExtractor(structuralNode);
            assertThat(extractor.getSource())
                    .isEqualTo("[id=\"a-new-section_{context}\"]\n" +
                               "= A new section\n" +
                               ":context: a-new-section");
        });
    }

    @Test
    public void testBasicParagraph() throws Exception {
        var adoc = "Some basic text in a Paragraph.\n" +
                   "With two lines.";

        var document = asciidoctor.load(adoc, optionsBuilder.asMap());
        var blocks = document.getBlocks();

        assertThat(blocks).hasSize(1);
        var extractor = new SourceExtractor(blocks.get(0));

        assertThat(extractor.getSource()).isEqualTo(adoc);
    }

    @Test
    public void testMoreAdvancedParagraph() throws Exception {
        var adoc = "Some basic text in a Paragraph.\n" +
                   "Here's some `text` with inline markup.\n" +
                   "And a https://google.com[link].\n" +
                   "Oh, and maybe an image::404.png[]";

        var document = asciidoctor.load(adoc, optionsBuilder.asMap());
        var blocks = document.getBlocks();

        assertThat(blocks).hasSize(1);
        var extractor = new SourceExtractor(blocks.get(0));

        assertThat(extractor.getSource()).isEqualTo(adoc);
    }

    @Test
    public void testSourceListing() throws Exception {
        var adoc = ".app.rb\n" +
                   "[source,ruby,subs=\"attributes\"]\n" +
                   "----\n" +
                   "require 'sinatra'\n" +
                   "\n" +
                   "get '/hi' do\n" +
                   "  \"Hello World!\"\n" +
                   "end\n" +
                   "----";

        var document = asciidoctor.load(adoc, optionsBuilder.asMap());
        var blocks = document.getBlocks();

        assertThat(blocks).hasSize(1);
        var extractor = new SourceExtractor(blocks.get(0));

        assertThat(extractor.getSource()).isEqualTo(adoc);
    }

    @Test
    public void testBasicTable() throws Exception {
        var adoc = "|===\n" +
                   "| Row one, Cell one | Row one, Cell two\n" +
                   "| Row two, Cell one | Row two, Cell two\n" +
                   "|===";

        var document = asciidoctor.load(adoc, optionsBuilder.asMap());
        var blocks = document.getBlocks();

        assertThat(blocks).hasSize(1);
        var extractor = new SourceExtractor(blocks.get(0));

        var expected = "[table,tablepcwidth=\"100\",rowcount=\"2\",colcount=\"2\"]\n" +
                       "|===\n" +
                       "| Row one, Cell one | Row one, Cell two\n" +
                       "| Row two, Cell one | Row two, Cell two\n" +
                       "|===";

        assertThat(extractor.getSource()).isEqualTo(expected);
    }

    // TODO: I need a more advanced table to test

    @Test
    public void testBasicOrderedList() throws Exception {
        var adoc = ". Protons\n" +
                   ". Electrons\n" +
                   ". Neutrons";

        var document = asciidoctor.load(adoc, optionsBuilder.asMap());
        var blocks = document.getBlocks();

        assertThat(blocks).hasSize(1);
        var extractor = new SourceExtractor(blocks.get(0));

        var expected = "[arabic]\n" +
                       ". Protons\n" +
                       ". Electrons\n" +
                       ". Neutrons";

        assertThat(extractor.getSource()).isEqualTo(expected);
    }

    @Test
    public void testBasicOrderedListWithNumbers() throws Exception {
        var adoc = "1. Protons\n" +
                   "2. Electrons\n" +
                   "3. Neutrons";

        var document = asciidoctor.load(adoc, optionsBuilder.asMap());
        var blocks = document.getBlocks();

        assertThat(blocks).hasSize(1);
        var extractor = new SourceExtractor(blocks.get(0));

        var expected = "[arabic]\n" +
                       ". Protons\n" +
                       ". Electrons\n" +
                       ". Neutrons";

        assertThat(extractor.getSource()).isEqualTo(expected);
    }

    @Test
    public void testAdvancedOrderedList() throws Exception {
        var adoc = "[%reversed,start=3]\n" +
                   ".Title\n" +
                   ". Protons\n" +
                   ". Electrons\n" +
                   ". Neutrons";

        var document = asciidoctor.load(adoc, optionsBuilder.asMap());
        var blocks = document.getBlocks();

        assertThat(blocks).hasSize(1);
        var extractor = new SourceExtractor(blocks.get(0));

        var expected = "[arabic,start=\"3\"]\n" +
                       ".Title\n" +
                       ". Protons\n" +
                       ". Electrons\n" +
                       ". Neutrons";

        assertThat(extractor.getSource()).isEqualTo(expected);
    }

    @Test
    public void testComplexListContent() throws Exception {
        var adoc = "* The header in AsciiDoc must start with a document title.\n" +
                   "+\n" +
                   "----\n" +
                   "= Document Title\n" +
                   "----\n" +
                   "+\n" +
                   "Keep in mind that the header is optional.\n" +
                   "\n" +
                   "* Optional Author and Revision information immediately follows the header title.\n" +
                   "+\n" +
                   "----\n" +
                   "= Document Title\n" +
                   "Doc Writer <doc.writer@asciidoc.org>\n" +
                   "v1.0, 2013-01-01\n" +
                   "----";

        var document = asciidoctor.load(adoc, optionsBuilder.asMap());
        var blocks = document.getBlocks();

        assertThat(blocks).hasSize(1);
        var extractor = new SourceExtractor(blocks.get(0));

        var expected = "* The header in AsciiDoc must start with a document title.\n" +
                       "+\n" +
                       "[listing]\n" +
                       "----\n" +
                       "= Document Title\n" +
                       "----\n" +
                       "+\n" +
                       "Keep in mind that the header is optional.\n" +
                       "* Optional Author and Revision information immediately follows the header title.\n" +
                       "+\n" +
                       "[listing]\n" +
                       "----\n" +
                       "= Document Title\n" +
                       "Doc Writer <doc.writer@asciidoc.org>\n" +
                       "v1.0, 2013-01-01\n" +
                       "----";

        assertThat(extractor.getSource()).isEqualTo(expected);
    }

    @Test
    public void testNestedOrderedList() throws Exception {
        var adoc = ". Protons\n" +
                   ".. Electrons\n" +
                   ". Neutrons";

        var document = asciidoctor.load(adoc, optionsBuilder.asMap());
        var blocks = document.getBlocks();

        assertThat(blocks).hasSize(1);
        var extractor = new SourceExtractor(blocks.get(0));

        var expected = "[arabic]\n" +
                       ". Protons\n" +
                       "[loweralpha]\n" +
                       ".. Electrons\n" +
                       ". Neutrons";

        assertThat(extractor.getSource()).isEqualTo(expected);
    }

    @Test
    public void testBasicUnorderedList() throws Exception {
        var adoc = "* Protons\n" +
                   "* Electrons\n" +
                   "* Neutrons";

        var document = asciidoctor.load(adoc, optionsBuilder.asMap());
        var blocks = document.getBlocks();

        assertThat(blocks).hasSize(1);
        var extractor = new SourceExtractor(blocks.get(0));

        var expected = "* Protons\n" +
                       "* Electrons\n" +
                       "* Neutrons";

        assertThat(extractor.getSource()).isEqualTo(expected);
    }

    @Test
    public void testDescriptionList() {
        var adoc = "CPU:: The brain of the computer.\n" +
                   "Hard drive:: Permanent storage for operating system and/or user files.\n" +
                   "RAM:: Temporarily stores information the CPU uses during operation.\n" +
                   "Keyboard:: Used to enter text or control items on the screen.\n" +
                   "Mouse:: Used to point to and select items on your computer screen.\n" +
                   "Monitor:: Displays information in visual form using text and graphics.";


        var document = asciidoctor.load(adoc, optionsBuilder.asMap());
        var blocks = document.getBlocks();

        assertThat(blocks).hasSize(1);
        var extractor = new SourceExtractor(blocks.get(0));

        assertThat(extractor.getSource()).isEqualTo(adoc);
    }

    @Test
    public void testMultipleTermDescriptionList() {
        var adoc = "term1::\n" +
                   "term2::\n" +
                   "def2";


        var document = asciidoctor.load(adoc, optionsBuilder.asMap());
        var blocks = document.getBlocks();

        assertThat(blocks).hasSize(1);
        var extractor = new SourceExtractor(blocks.get(0));

        var expected = "term1::\n" +
                       "term2:: def2";

        assertThat(extractor.getSource()).isEqualTo(expected);
    }

    @Test
    public void testBlockInDescriptionList() {
        var adoc = "a-term::\n" +
                   "term::\n" +
                   "+\n" +
                   "....\n" +
                   "literal, line 1\n" +
                   "literal, line 2\n" +
                   "....\n" +
                   "anotherterm:: def";


        var document = asciidoctor.load(adoc, optionsBuilder.asMap());
        var blocks = document.getBlocks();

        assertThat(blocks).hasSize(1);
        var extractor = new SourceExtractor(blocks.get(0));

        var expected = "a-term::\n" +
                       "term::\n" +
                       "+\n" +
                       "[literal]\n" +
                       "....\n" +
                       "literal, line 1\n" +
                       "literal, line 2\n" +
                       "....\n" +
                       "anotherterm:: def";

        assertThat(extractor.getSource()).isEqualTo(expected);
    }

    @Test
    public void testSourceBlock() {
        var adoc = "[source,java]\n" +
                   "====\n" +
                   "public class HelloWorld { // <1>\n" +
                   "    public static void main(String... args) {\n" +
                   "        System.out.println(\"Hello World\"); // <2>\n" +
                   "    }\n" +
                   "}\n" +
                   "====";

        var document = asciidoctor.load(adoc, optionsBuilder.asMap());
        var blocks = document.getBlocks();

        assertThat(blocks).hasSize(1);
        var extractor = new SourceExtractor(blocks.get(0));

        assertThat(extractor.getSource()).isEqualTo(adoc);
    }

    @Test
    public void testCalloutList() {
        var adoc = "<1> First Java Class\n" +
                   "<2> Output to standard out";

        var document = asciidoctor.load(adoc, optionsBuilder.asMap());
        var blocks = document.getBlocks();

        assertThat(blocks).hasSize(1);
        var extractor = new SourceExtractor(blocks.get(0));
        var expected = "[arabic]\n" +
                       "<1> First Java Class\n" +
                       "<2> Output to standard out";

        assertThat(extractor.getSource()).isEqualTo(expected);
    }

    @Test
    public void testImageBlock() {
        var adoc = ".A mountain sunset\n" +
                   "[#img-sunset]\n" +
                   "[caption=\"Figure 1: \",link=https://www.flickr.com/photos/javh/5448336655]\n" +
                   "image::sunset.jpg[Sunset,300,200]";

        var document = asciidoctor.load(adoc, optionsBuilder.asMap());
        var blocks = document.getBlocks();

        assertThat(blocks).hasSize(1);
        var extractor = new SourceExtractor(blocks.get(0));
        var expected = ".A mountain sunset\n" +
                       "[id=\"img-sunset_{context}\", caption=\"Figure 1: \",link=\"https://www.flickr.com/photos/javh/5448336655\",alt=\"Sunset\",width=\"300\",height=\"200\"]\n" +
                       "image::sunset.jpg[]";

        assertThat(extractor.getSource()).isEqualTo(expected);
    }

    @Test
    public void testVideoBlock() {
        var adoc = ".An ocean sunset\n" +
                   "video::rPQoq7ThGAU[youtube,300,450, start=20, end=90, theme=light, lang=en, opts=\"autoplay,loop,nofullscreen\"]";

        var document = asciidoctor.load(adoc, optionsBuilder.asMap());
        var blocks = document.getBlocks();

        assertThat(blocks).hasSize(1);
        var extractor = new SourceExtractor(blocks.get(0));
        var expected = ".An ocean sunset\n" +
                       "[start=\"20\",width=\"300\",end=\"90\",theme=\"light\",lang=\"en\",poster=\"youtube\",height=\"450\"]\n" +
                       "video::rPQoq7ThGAU[opts=\"autoplay,nofullscreen,loop\"]";

        assertThat(extractor.getSource()).isEqualTo(expected);
    }

    @Test
    public void testThematicBreak() {
        var adoc = "'''";

        var document = asciidoctor.load(adoc, optionsBuilder.asMap());
        var blocks = document.getBlocks();

        assertThat(blocks).hasSize(1);
        var extractor = new SourceExtractor(blocks.get(0));

        assertThat(extractor.getSource()).isEqualTo(adoc);
    }

    @Test
    public void testPageBreak() {
        var adoc = "<<<";

        var document = asciidoctor.load(adoc, optionsBuilder.asMap());
        var blocks = document.getBlocks();

        assertThat(blocks).hasSize(1);
        var extractor = new SourceExtractor(blocks.get(0));

        assertThat(extractor.getSource()).isEqualTo(adoc);
    }
}
