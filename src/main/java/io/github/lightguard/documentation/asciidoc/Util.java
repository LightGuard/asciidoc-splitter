package io.github.lightguard.documentation.asciidoc;

import io.github.lightguard.documentation.asciidoc.extension.ReaderPreprocessor;
import org.asciidoctor.ast.ContentNode;

import java.util.regex.Pattern;

public class Util {

    public static final String MODULE_TYPE_ATTRIBUTE = "module-type";

    public static String getFullId(ContentNode node) {
        StringBuilder buf = new StringBuilder(node.getId());

        while (node.getParent() != null) {
            buf.insert(0, node.getId() + "/");
            node = node.getParent();
        }

        return buf.toString();
    }

    public static String getModuleType(ContentNode node) {
        if (node.getAttributes().containsKey(MODULE_TYPE_ATTRIBUTE))
            return node.getAttributes().get(MODULE_TYPE_ATTRIBUTE).toString();

        // Get the Id of the node, and strip off "_{context}" if it exists
        var id = node.getId().replace("_{context}", "");

        if (id.startsWith("con-") || id.endsWith("-con"))
            return "con";

        if (id.startsWith("proc-") || id.endsWith("-proc"))
            return "proc";

        if (id.startsWith("ref-") || id.endsWith("-ref"))
            return "ref";

        return "unknown"; // punt, we don't know
    }

    public static String fixIncludes(String source, boolean shouldFixModules) {
        var sourceFixed = source.replaceAll("(?<include>include::)(?<path>(\\w|/|-)*)?chap-(?<filename>.+)\\.(?<extension>.+)\\[]",
                        "${include}assemblies/assembly-${filename}.${extension}[]")
                .replaceAll("(?<include>include::)(?<path>(\\w|/|-)*)?assembly-(?<filename>.+)\\.(?<extension>.+)\\[]",
                        "${include}assemblies/assembly-${filename}.${extension}[]")
                .replaceAll("(?<include>include::)(\\{asciidoc-dir}/)?(?<path>(\\w|/|-)*)/(?<filename>.*)\\[tags=(?<module>.+)]",
                        "${include}modules/${path}/${module}.adoc[leveloffset=+1]");
        if (shouldFixModules) {
            sourceFixed = sourceFixed.replaceAll("(?<include>include::)(?<filename>[\\p{Alpha}\\-]+\\.adoc)\\[(?<opts>.*)]",
                    "${include}modules/${filename}[${opts}]");
        }
        return sourceFixed;
    }

    public static String fixIncludes(String source) {
        return fixIncludes(source, true);
    }

    public static String fixModuleInclude(String source) {
        var pattern = Pattern.compile("(?<include>include::)(?<path>(\\w|/|-)*)/(?<filename>.+)\\[(?<params>.+)]");
        var matcher = pattern.matcher(source);
        if (matcher.matches()) {
            var params = "null".equals(matcher.group("params")) ? "" : matcher.group("params");

            // Getting a little lazy with the second replacement, but it seems to be the correct fix.
            return matcher.replaceAll("${include}modules/${path}/${filename}[" + params + "]")
                          .replaceAll("modules/modules/", "../../modules/");
        }
        return source;
    }

    /**
     * Removes the splitter comment (to step around ifdef) from the source, then returns.
     *
     * @param source source line
     * @return source line without splitter comment
     */
    public static String removeSplitterComment(String source) {
        return source.replaceAll(ReaderPreprocessor.SPLITTER_COMMENT, "");
    }

    /**
     * Replaces "chap-" with "assembly-"
     *
     * @param source source line
     * @return source line with "chap-" to "assembly-" tweak
     */
    public static String replaceChapWithAssembly(String source) {
        return source.replaceAll("chap-", "assembly-");
    }

    /**
     * A wrapper method for all the tweaks and fixes to asciidoc source.
     * @param source Pre-tweaked source
     * @return Source with tweaks/fixes applied
     */
    public static String tweakSource(String source) {
        return fixModuleInclude(fixIncludes(removeSplitterComment(replaceChapWithAssembly(source))));
    }

    // TODO: issue-88
    public static String fixSectionLevelForModule(String source, int offset) {
        // Fix up section levels for output
        var pattern = Pattern.compile("^(?<sectionLevel>=+)\\s(?<title>(?:\\w|\\s|\\p{Punct})+)");
        var matcher = pattern.matcher(source);

        if (matcher.matches()) {
            var sectionLevel = matcher.group("sectionLevel");
            return sectionLevel.substring(0, sectionLevel.length() - offset) + " " + matcher.group("title");
        }

        return source;
    }

    public static CharSequence fixForPv2(String source) {
        var offset = source.indexOf("\n\n") + 1; // We need the character AFTER what indexOf returns

        var newSource = new StringBuilder(source);
        newSource.insert(offset, ":imagesdir: _images\n");
        newSource.append("\n");

        return tweakSource(newSource.toString());
    }
}
