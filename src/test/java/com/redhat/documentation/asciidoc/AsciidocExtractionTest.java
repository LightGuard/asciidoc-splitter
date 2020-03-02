package com.redhat.documentation.asciidoc;

import java.io.File;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.junit.jupiter.api.BeforeEach;

public abstract class AsciidocExtractionTest {
    Asciidoctor asciidoctor;
    OptionsBuilder optionsBuilder;

    @BeforeEach
    void asciidoctorSetup() throws Exception {
        optionsBuilder = OptionsBuilder.options();
        asciidoctor = Asciidoctor.Factory.create();

        // We need access to the line numbers and source
        optionsBuilder.sourcemap(true);
    }
}
