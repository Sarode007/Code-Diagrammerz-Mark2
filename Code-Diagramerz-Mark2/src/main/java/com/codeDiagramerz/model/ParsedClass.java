package com.codeDiagramerz.model;



import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.codeDiagramerz.parser.MethodUtils;
import com.codeDiagramerz.parser.TypeUtils;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ObjectCreationExpr;

public class ParsedClass {
    private final String name;
    private final String typeLabel;
    private final List<String> fields = new ArrayList<>();
    private final List<String> userMethods = new ArrayList<>();
    private final List<String> systemMethods = new ArrayList<>();
    private final List<String> getterSetters = new ArrayList<>();
    private final List<String> constructors = new ArrayList<>();
    private final String annotations;
    private final String comment;

    public ParsedClass(ClassOrInterfaceDeclaration cls,
                       Map<String, Set<String>> classDeps,
                       Map<String, Set<String>> inheritance,
                       Map<String, Set<String>> interfaces) {

        this.name = cls.getNameAsString();

        if (cls.isInterface()) {
            this.typeLabel = "🅘Interface";
        } else if (cls.isAbstract()) {
            this.typeLabel = "🅰Abstract Class";
        } else {
            this.typeLabel = "🅲Class";
        }

        this.annotations = cls.getAnnotations().stream()
                .map(a -> "@" + a.getNameAsString())
                .collect(Collectors.joining(" "));
        this.comment = cls.getJavadoc().map(jd -> jd.getDescription().toText()).orElse("");

        List<String> fieldTypes = new ArrayList<>();
        Set<String> deps = new HashSet<>();

        // Fields
        for (FieldDeclaration field : cls.getFields()) {
            String modifier = field.isPrivate() ? "🔒" :
                              (field.isPublic() ? "🟢" :
                              (field.isProtected() ? "🛡️" : "🅾️"));

            String fieldAnnotations = field.getAnnotations().stream()
                    .map(a -> "@" + a.getNameAsString())
                    .collect(Collectors.joining(" "));

            field.getVariables().forEach(var -> {
                String type = TypeUtils.sanitizeType(var.getTypeAsString());
                String name = var.getNameAsString();
                String fieldStr = "  " + modifier + " " + type + " " + name;
                if (!fieldAnnotations.isEmpty()) {
                    fieldStr = "  " + fieldAnnotations + " " + fieldStr.trim();
                }
                fields.add(fieldStr);
                fieldTypes.add(type);
            });
        }

        // Methods
        for (MethodDeclaration method : cls.getMethods()) {
            String modifier = method.isPrivate() ? "🔒" :
                              (method.isPublic() ? "🟢" :
                              (method.isProtected() ? "🛡️" : "~"));

            String returnType = TypeUtils.sanitizeType(method.getTypeAsString());
            String methodName = method.getNameAsString();

            String methodAnnotations = method.getAnnotations().stream()
                    .map(a -> "@" + a.getNameAsString())
                    .collect(Collectors.joining(" "));

            String paramList = method.getParameters().stream()
                    .map(p -> TypeUtils.sanitizeType(p.getTypeAsString()) + " " + p.getNameAsString())
                    .collect(Collectors.joining(", "));

            String signature = modifier + " " + returnType + " " + methodName + "(" + paramList + ")";
            if (!methodAnnotations.isEmpty()) {
                signature = methodAnnotations + " " + signature;
            }

            boolean isOverride = method.isAnnotationPresent("Override");
            boolean isGetterSetter = MethodUtils.isGetterSetter(method);
            boolean isSystem = methodName.equals("toString") || methodName.equals("hashCode") || methodName.equals("equals");

            if (isGetterSetter) {
                getterSetters.add("🔘 " + signature);
            } else if (isSystem && isOverride) {
                systemMethods.add("🔁🏛 " + signature);
            } else if (isSystem) {
                systemMethods.add("🏛 " + signature);
            } else if (isOverride) {
                systemMethods.add("🔁 " + signature);
            } else {
                userMethods.add("🛠 " + signature);
            }

            method.findAll(ObjectCreationExpr.class).forEach(obj -> {
                String depClass = TypeUtils.sanitizeType(obj.getTypeAsString());
                if (!depClass.equals(this.name)) deps.add(depClass);
            });
        }

        // Constructors
        for (ConstructorDeclaration ctor : cls.getConstructors()) {
            String modifier = ctor.isPrivate() ? "🔒" :
                              (ctor.isPublic() ? "🟢" :
                              (ctor.isProtected() ? "🛡️" : "~"));

            String ctorAnnotations = ctor.getAnnotations().stream()
                    .map(a -> "@" + a.getNameAsString())
                    .collect(Collectors.joining(" "));

            String paramList = ctor.getParameters().stream()
                    .map(p -> TypeUtils.sanitizeType(p.getTypeAsString()) + " " + p.getNameAsString())
                    .collect(Collectors.joining(", "));

            String signature = modifier + " " + this.name + "(" + paramList + ")";
            if (!ctorAnnotations.isEmpty()) {
                signature = ctorAnnotations + " " + signature;
            }
            constructors.add("⚙ " + signature);
        }

        classDeps.put(this.name, deps);
        classDeps.get(this.name).addAll(fieldTypes);

        cls.getExtendedTypes().forEach(ext ->
                inheritance.computeIfAbsent(this.name, k -> new HashSet<>()).add(TypeUtils.sanitizeType(ext.getNameAsString()))
        );

        cls.getImplementedTypes().forEach(impl ->
                interfaces.computeIfAbsent(this.name, k -> new HashSet<>()).add(TypeUtils.sanitizeType(impl.getNameAsString()))
        );
    }

    public String getName() {
        return name;
    }
    public String getTypeLabel() {
        return typeLabel;
    }
    public String getAnnotations() {
        return annotations;
    }
    public String getComment() {
        return comment;
    }
    public List<String> getFields() {
        return fields;
    }
    public List<String> getConstructors() {
        return constructors;
    }
    public List<String> getGetterSetters() {
        return getterSetters;
    }
    public List<String> getUserMethods() {
        return userMethods;
    }
    public List<String> getSystemMethods() {
        return systemMethods;
    }
}

