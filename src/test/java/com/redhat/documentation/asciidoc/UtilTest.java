package com.redhat.documentation.asciidoc;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UtilTest {
    @Test
    public void testFixIncludesChapter() {
        var testString = "include::chap-kogito-using-dmn-models.adoc[]";
        var expected = "include::assemblies/assembly-kogito-using-dmn-models.adoc[]";

        assertThat(Util.fixIncludes(testString)).isEqualTo(expected);
    }

    @Test
    public void testFixIncludesTags() {
        var testString = "include::{asciidoc-dir}/creating-running/chap-kogito-creating-running.adoc[tags=ref-kogito-app-examples]";
        var expected = "include::modules/creating-running/ref-kogito-app-examples.adoc[leveloffset=+1]";

        assertThat(Util.fixIncludes(testString)).isEqualTo(expected);
    }
}
