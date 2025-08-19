package com.codeDiagramerz.parser;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.codeDiagramerz.model.ParsedClass;
import com.codeDiagramerz.model.ParsedEnum;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;

public class EnhancedJavaParser {
    private final Map<String, ParsedClass> parsedClasses = new LinkedHashMap<>();
    private final Map<String, ParsedEnum> parsedEnums = new LinkedHashMap<>();
    private final Map<String, Set<String>> classDeps = new HashMap<>();
    private final Map<String, Set<String>> inheritance = new HashMap<>();
    private final Map<String, Set<String>> interfaces = new HashMap<>();

    public void parsePath(File path) {
        List<File> javaFiles = new ArrayList<>();
        if (path.isDirectory()) {
            FileScannerUtil.findAllJavaFiles(path, javaFiles);
        } else if (path.getName().endsWith(".java")) {
            javaFiles.add(path);
        }

        for (File file : javaFiles) {
            try {
                CompilationUnit cu = StaticJavaParser.parse(file);

                // Enums
                for (EnumDeclaration enumDecl : cu.findAll(EnumDeclaration.class)) {
                    ParsedEnum parsedEnum = new ParsedEnum(enumDecl);
                    parsedEnums.put(parsedEnum.getName(), parsedEnum);
                }

                // Classes / Interfaces
                for (ClassOrInterfaceDeclaration cls : cu.findAll(ClassOrInterfaceDeclaration.class)) {
                    ParsedClass parsedClass = new ParsedClass(cls, classDeps, inheritance, interfaces);
                    parsedClasses.put(parsedClass.getName(), parsedClass);
                }
            } catch (Exception e) {
                System.err.println("⚠️ Error parsing file: " + file.getAbsolutePath());
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    public Map<String, ParsedClass> getParsedClasses() {
        return parsedClasses;
    }

    public Map<String, ParsedEnum> getParsedEnums() {
        return parsedEnums;
    }

    public Map<String, Set<String>> getClassDeps() {
        return classDeps;
    }

    public Map<String, Set<String>> getInheritance() {
        return inheritance;
    }

    public Map<String, Set<String>> getInterfaces() {
        return interfaces;
    }
}
