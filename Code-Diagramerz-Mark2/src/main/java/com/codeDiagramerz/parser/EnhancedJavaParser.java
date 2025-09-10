package com.codeDiagramerz.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.codeDiagramerz.model.ParsedClass;
import com.codeDiagramerz.model.ParsedEnum;
import com.codeDiagramerz.model.ParsedMethod;
import com.codeDiagramerz.model.ParsedMethod.MethodCall;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;

public class EnhancedJavaParser {
    private final Map<String, ParsedClass> parsedClasses = new LinkedHashMap<>();
    private final Map<String, ParsedEnum> parsedEnums = new LinkedHashMap<>();
    private final Map<String, Set<String>> classDeps = new HashMap<>();
    private final Map<String, Set<String>> inheritance = new HashMap<>();
    private final Map<String, Set<String>> interfaces = new HashMap<>();
    private final Map<String, List<ParsedMethod>> methodDetails = new HashMap<>();

    public void parsePath(File path) {
        List<File> javaFiles = new ArrayList<>();
        javaFiles.clear();
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
                    
                    // Parse methods for sequence diagrams
                    parseMethodDetails(cls);
                }

            } catch (Exception e) {
                System.err.println("⚠️ Error parsing file: " + file.getAbsolutePath());
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    private void parseMethodDetails(ClassOrInterfaceDeclaration cls) {
        String className = cls.getNameAsString();
        List<ParsedMethod> methods = new ArrayList<>();

        // Parse regular methods
        for (MethodDeclaration method : cls.getMethods()) {
            String visibility = getVisibility(method.isPrivate(), method.isPublic(), method.isProtected());
            String returnType = TypeUtils.sanitizeType(method.getTypeAsString());
            List<String> parameters = method.getParameters().stream()
                    .map(p -> TypeUtils.sanitizeType(p.getTypeAsString()))
                    .collect(Collectors.toList());

            ParsedMethod parsedMethod = new ParsedMethod(
                className, method.getNameAsString(), returnType, parameters, visibility, false);

            // Parse method calls within this method
            parseMethodCalls(method, parsedMethod);
            methods.add(parsedMethod);
        }

        // Parse constructors
        for (ConstructorDeclaration ctor : cls.getConstructors()) {
            String visibility = getVisibility(ctor.isPrivate(), ctor.isPublic(), ctor.isProtected());
            List<String> parameters = ctor.getParameters().stream()
                    .map(p -> TypeUtils.sanitizeType(p.getTypeAsString()))
                    .collect(Collectors.toList());

            ParsedMethod parsedMethod = new ParsedMethod(
                className, className, "void", parameters, visibility, true);

            // Parse method calls within constructor
            parseConstructorCalls(ctor, parsedMethod);
            methods.add(parsedMethod);
        }

        methodDetails.put(className, methods);
    }

    private void parseMethodCalls(MethodDeclaration method, ParsedMethod parsedMethod) {
        // Find method calls
        method.findAll(MethodCallExpr.class).forEach(call -> {
            String methodName = call.getNameAsString();
            String targetClass = determineTargetClass(call);
            int lineNumber = call.getRange().map(r -> r.begin.line).orElse(0);
            
            parsedMethod.addMethodCall(new MethodCall(targetClass, methodName, "method", lineNumber));
        });

        // Find object creation
        method.findAll(ObjectCreationExpr.class).forEach(obj -> {
            String targetClass = TypeUtils.sanitizeType(obj.getTypeAsString());
            int lineNumber = obj.getRange().map(r -> r.begin.line).orElse(0);
            
            parsedMethod.addMethodCall(new MethodCall(targetClass, targetClass, "constructor", lineNumber));
        });
    }

    private void parseConstructorCalls(ConstructorDeclaration ctor, ParsedMethod parsedMethod) {
        // Find method calls in constructor
        ctor.findAll(MethodCallExpr.class).forEach(call -> {
            String methodName = call.getNameAsString();
            String targetClass = determineTargetClass(call);
            int lineNumber = call.getRange().map(r -> r.begin.line).orElse(0);
            
            parsedMethod.addMethodCall(new MethodCall(targetClass, methodName, "method", lineNumber));
        });

        // Find object creation in constructor
        ctor.findAll(ObjectCreationExpr.class).forEach(obj -> {
            String targetClass = TypeUtils.sanitizeType(obj.getTypeAsString());
            int lineNumber = obj.getRange().map(r -> r.begin.line).orElse(0);
            
            parsedMethod.addMethodCall(new MethodCall(targetClass, targetClass, "constructor", lineNumber));
        });
    }

    private String determineTargetClass(MethodCallExpr call) {
        // Try to determine the target class from the method call
        if (call.getScope().isPresent()) {
            String scope = call.getScope().get().toString();
            // This is simplified - in a real implementation, you'd need more sophisticated type resolution
            if (scope.contains(".")) {
                return scope.split("\\.")[0];
            }
            return scope;
        }
        return "this"; // Method call on current class
    }

    private String getVisibility(boolean isPrivate, boolean isPublic, boolean isProtected) {
        if (isPrivate) return "🔒";
        if (isPublic) return "🟢";
        if (isProtected) return "🛡️";
        return "🅾️";
    }

    // Existing getters
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

    // New getter for method details
    public Map<String, List<ParsedMethod>> getMethodDetails() {
        return methodDetails;
    }
}




// mark-1  (working for class diagram perfectly)

//package com.codeDiagramerz.parser;
//import java.io.File;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import com.codeDiagramerz.model.ParsedClass;
//import com.codeDiagramerz.model.ParsedEnum;
//import com.github.javaparser.StaticJavaParser;
//import com.github.javaparser.ast.CompilationUnit;
//import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
//import com.github.javaparser.ast.body.EnumDeclaration;
//
//public class EnhancedJavaParser {
//    private final Map<String, ParsedClass> parsedClasses = new LinkedHashMap<>();
//    private final Map<String, ParsedEnum> parsedEnums = new LinkedHashMap<>();
//    private final Map<String, Set<String>> classDeps = new HashMap<>();
//    private final Map<String, Set<String>> inheritance = new HashMap<>();
//    private final Map<String, Set<String>> interfaces = new HashMap<>();
//
//    public void parsePath(File path) {
//        List<File> javaFiles = new ArrayList<>();
//        javaFiles.clear();
//        if (path.isDirectory()) {
//            FileScannerUtil.findAllJavaFiles(path, javaFiles);
//        } else if (path.getName().endsWith(".java")) {
//            javaFiles.add(path);
//        }
//
//        for (File file : javaFiles) {
//            try {
//                CompilationUnit cu = StaticJavaParser.parse(file);
//
//                // Enums
//                for (EnumDeclaration enumDecl : cu.findAll(EnumDeclaration.class)) {
//                    ParsedEnum parsedEnum = new ParsedEnum(enumDecl);
//                    parsedEnums.put(parsedEnum.getName(), parsedEnum);
//                }
//
//                // Classes / Interfaces
//                for (ClassOrInterfaceDeclaration cls : cu.findAll(ClassOrInterfaceDeclaration.class)) {
//                    ParsedClass parsedClass = new ParsedClass(cls, classDeps, inheritance, interfaces);
//                    parsedClasses.put(parsedClass.getName(), parsedClass);
//                }
//
//            } catch (Exception e) {
//                System.err.println("⚠️ Error parsing file: " + file.getAbsolutePath());
//                System.err.println("Error: " + e.getMessage());
//            }
//        }
//        
//    }
//
//    public Map<String, ParsedClass> getParsedClasses() {
//        return parsedClasses;
//    }
//
//    public Map<String, ParsedEnum> getParsedEnums() {
//        return parsedEnums;
//    }
//
//    public Map<String, Set<String>> getClassDeps() {
//        return classDeps;
//    }
//
//    public Map<String, Set<String>> getInheritance() {
//        return inheritance;
//    }
//
//    public Map<String, Set<String>> getInterfaces() {
//        return interfaces;
//    }
//}
