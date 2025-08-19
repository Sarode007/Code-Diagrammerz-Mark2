package com.codeDiagramerz.parser;


import com.github.javaparser.ast.body.MethodDeclaration;

public class MethodUtils {
    public static boolean isGetterSetter(MethodDeclaration method) {
        String name = method.getNameAsString();
        if (method.getParameters().size() > 0) {
            return name.startsWith("set");
        } else {
            return name.startsWith("get") || name.startsWith("is");
        }
    }
}
