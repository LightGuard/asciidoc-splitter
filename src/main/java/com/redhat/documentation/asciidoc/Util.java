package com.redhat.documentation.asciidoc;

import org.asciidoctor.ast.ContentNode;

public class Util {

    public static final String MODULE_TYPE_ATTRIBUTE = "module-type";
    public static final String ASSETS_LOCATION = "../_assets";

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

        var id = node.getId();

        if (id.startsWith("con-"))
            return "con";

        if (id.startsWith("proc-"))
            return "proc";

        if (id.startsWith("ref-"))
            return "ref";

        return "unknown"; // punt, we don't know
    }

    public static String fixAsset(String line) {
        return line
                .replaceAll("(video|audio)::(\\w+)\\.(\\w+)\\[(.*)]", "$1::" + ASSETS_LOCATION + "/$2.$3[$4]");
    }
}
