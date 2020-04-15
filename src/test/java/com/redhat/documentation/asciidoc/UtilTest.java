package com.redhat.documentation.asciidoc;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class UtilTest {

    @Test
    public void fixVideoAsset() {
        var adoc = "Hello video::world.mp4[width=640, start=60, end=140, options=autoplay]";
        var expected = "Hello video::../_assets/world.mp4[width=640, start=60, end=140, options=autoplay]";

        assertThat(Util.fixAsset(adoc)).isEqualTo(expected);
    }

    @Test
    public void fixVideoAssetYouTube() {
        var adoc = "Hello video::rPQoq7ThGAU[youtube]";
        var expected = "Hello video::rPQoq7ThGAU[youtube]";

        assertThat(Util.fixAsset(adoc)).isEqualTo(expected);
    }

    @Test
    public void fixAudioAsset() {
        var adoc = "Listen to the audio::ocean_waves.mp3[options=\"autoplay,loop\"]";
        var expected = "Listen to the audio::../_assets/ocean_waves.mp3[options=\"autoplay,loop\"]";

        assertThat(Util.fixAsset(adoc)).isEqualTo(expected);
    }

    @Test
    public void fixNestedAudioAsset() {
        var adoc = "Listen to the audio::debugging/ocean_waves.mp3[options=\"autoplay,loop\"]";
        var expected = "Listen to the audio::../_assets/debugging/ocean_waves.mp3[options=\"autoplay,loop\"]";

        assertThat(Util.fixAsset(adoc)).isEqualTo(expected);
    }

    @Test
    public void fixNestedVideoAsset() {
        var adoc = "Hello video::debugging/video/world.mp4[width=640, start=60, end=140, options=autoplay]";
        var expected = "Hello video::../_assets/debugging/video/world.mp4[width=640, start=60, end=140, options=autoplay]";

        assertThat(Util.fixAsset(adoc)).isEqualTo(expected);
    }
}
