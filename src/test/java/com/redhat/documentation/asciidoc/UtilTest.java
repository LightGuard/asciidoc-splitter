package com.redhat.documentation.asciidoc;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UtilTest {
    @Test
    public void testFixIncludesChapter() {
        var testString = "include::chap-kogito-using-dmn-models.adoc[]";
        var expected = "include::assemblies/assembly-kogito-using-dmn-models.adoc[]";

        assertThat(Util.fixIncludes(testString)).isEqualTo(expected);
        assertThat(Util.tweakSource(testString)).isEqualTo(expected);
    }

    @Test
    public void testFixIncludesTags() {
        var testString = "include::{asciidoc-dir}/creating-running/chap-kogito-creating-running.adoc[tags=ref-kogito-app-examples]";
        var expected = "include::modules/creating-running/ref-kogito-app-examples.adoc[leveloffset=+1]";

        assertThat(Util.fixIncludes(testString)).isEqualTo(expected);
    }

    @Test
    public void testFixSectionLevelForModule() {
        var test = "==== Some sort of title";
        var expected = "=== Some sort of title";

        assertThat(Util.fixSectionLevelForModule(test)).isEqualTo(expected);
    }

    @Test
    public void testFixSectionLevelForModuleWithPunctuation() {
        var test = "==== Some sort of title {PRODUCT}";
        var expected = "=== Some sort of title {PRODUCT}";

        assertThat(Util.fixSectionLevelForModule(test)).isEqualTo(expected);

        test = "=== GraphQL queries for process instances and user task instances (instance caches)";
        expected = "== GraphQL queries for process instances and user task instances (instance caches)";
        assertThat(Util.fixSectionLevelForModule(test)).isEqualTo(expected);
    }
}
