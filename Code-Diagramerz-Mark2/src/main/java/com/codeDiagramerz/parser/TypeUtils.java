package com.codeDiagramerz.parser;

public class TypeUtils {
    public static String sanitizeType(String type) {
        return type.replaceAll("<.*>", "")
                   .replaceAll("\\[\\]", "")
                   .replaceAll("@.*", "");
    }
}
