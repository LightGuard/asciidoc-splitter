package com.redhat.documentation.asciidoc;

import org.asciidoctor.ast.ContentNode;

public class Util {

        static public String getFullId(ContentNode node) {
            StringBuilder buf = new StringBuilder(node.getId());

            while(node.getParent()!=null) {
                buf.insert(0, node.getId() + "/");
                node = node.getParent();
            }

            return buf.toString();
        }
}
