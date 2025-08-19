package com.codeDiagramerz.model;

import java.util.List;
import java.util.stream.Collectors;

import com.github.javaparser.ast.body.EnumDeclaration;

public class ParsedEnum {
    private final String name;
    private final List<String> values;

    public ParsedEnum(EnumDeclaration enumDecl) {
        this.name = enumDecl.getNameAsString();
        this.values = enumDecl.getEntries()
                              .stream()
                              .map(e -> e.getNameAsString())
                              .collect(Collectors.toList());
    }

    public String getName() {
        return name;
    }
    public List<String> getValues() {
        return values;
    }
}
