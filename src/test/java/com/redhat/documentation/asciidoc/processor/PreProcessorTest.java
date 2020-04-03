package com.redhat.documentation.asciidoc.processor;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.redhat.documentation.asciidoc.extraction.AsciidocExtractionTest;
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.PreprocessorReader;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class PreProcessorTest extends AsciidocExtractionTest {
    public static class MyPreProcessor extends Preprocessor {

        private List<String> lines;

        @Override
        public void process(Document document, PreprocessorReader reader) {
            lines = reader.lines();
        }

        public List<String> getLines() {
            return Collections.unmodifiableList(lines);
        }
    }

    @Test
    @Disabled
    public void findAttributesTest() throws Exception {
        var preprocessorInstance = new MyPreProcessor();
        asciidoctor.javaExtensionRegistry().preprocessor(preprocessorInstance);

        var adoc = Files.readString(Paths.get("./examples/sample/input"));

        var doc = asciidoctor.load(adoc, optionsBuilder.asMap());
        var para = doc.findBy(Map.of("context", ":section"));
        para.get(0);

//        var newSection = para.get(1);
//        var subSection = para.get(2);
//
//        subSection.getSourceLocation().getLineNumber();
//        We should be able to get all the lines for a section
//        by getting the next section and going up until we find an empty line, that will be the end of the current section
//        We can then use that as the source
    }
}
