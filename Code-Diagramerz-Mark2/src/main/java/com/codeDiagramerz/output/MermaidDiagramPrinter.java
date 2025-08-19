// Only prints Meramid Syntax
package com.codeDiagramerz.output;

import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;

import com.codeDiagramerz.model.ParsedClass;
import com.codeDiagramerz.model.ParsedEnum;

public class MermaidDiagramPrinter {

	    private final Map<String, ParsedClass> classes;
	    private final Map<String, ParsedEnum> enums;
	    private final Map<String, Set<String>> deps;
	    private final Map<String, Set<String>> inheritance;
	    private final Map<String, Set<String>> interfaces;

	    public MermaidDiagramPrinter(Map<String, ParsedClass> classes,
	                                 Map<String, ParsedEnum> enums,
	                                 Map<String, Set<String>> deps,
	                                 Map<String, Set<String>> inheritance,
	                                 Map<String, Set<String>> interfaces) {
	        this.classes = classes;
	        this.enums = enums;
	        this.deps = deps;
	        this.inheritance = inheritance;
	        this.interfaces = interfaces;
	    }

    public void printDiagram(PrintWriter out) {
        out.println("classDiagram");

        // Enums
        enums.values().forEach(e -> {
            out.println("class " + e.getName() + " {");
            out.println("<<enumeration>>");
            e.getValues().forEach(v -> out.println("  " + v));
            out.println("}");
        });

        // Classes
        classes.values().forEach(c -> {
            out.println("class " + c.getName() + " {");
            out.println("  " + c.getTypeLabel());
            if (!c.getAnnotations().isEmpty()) {
                out.println("  " + c.getAnnotations());
            }
            if (!c.getComment().isEmpty()) {
                String comment = c.getComment().length() > 50 ? c.getComment().substring(0, 47) + "..." : c.getComment();
                out.println("  %% " + comment.replace("\n", " "));
            }
            c.getFields().forEach(out::println);
            if (!c.getConstructors().isEmpty()) {
                out.println("%% ⚙ Constructors");
                c.getConstructors().forEach(out::println);
            }
            if (!c.getGetterSetters().isEmpty()) {
                out.println("%% 🔘 Getters/Setters");
                c.getGetterSetters().forEach(out::println);
            }
            if (!c.getUserMethods().isEmpty()) {
                out.println("%% 🛠 User-Defined Methods");
                c.getUserMethods().forEach(out::println);
            }
            if (!c.getSystemMethods().isEmpty()) {
                out.println("%% 🏛 System/Overridden Methods");
                c.getSystemMethods().forEach(out::println);
            }
            out.println("}");
        });

        // Dependencies
        deps.forEach((from, toSet) -> {
            toSet.forEach(to -> {
                if ((classes.containsKey(to) || enums.containsKey(to)) && !to.equals(from)) {
                    out.println(from + " --> " + to + " : uses");
                }
            });
        });

        // Inheritance
        inheritance.forEach((sub, supers) ->
            supers.forEach(s -> {
                if (classes.containsKey(s)) {
                    out.println(sub + " --|> " + s + " : extends");
                }
            })
        );

        // Interfaces
        interfaces.forEach((impl, ifaces) ->
            ifaces.forEach(i -> {
                if (classes.containsKey(i)) {
                    out.println(impl + " ..|> " + i + " : implements");
                }
            })
        );
    }
}
