package com.codeDiagramerz.output;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.codeDiagramerz.model.ParsedMethod;
import com.codeDiagramerz.model.ParsedMethod.MethodCall;

public class SequenceDiagramPrinter {
    
    private final Map<String, List<ParsedMethod>> classMethods;
    private final Set<String> knownClasses;

    public SequenceDiagramPrinter(Map<String, List<ParsedMethod>> classMethods, Set<String> allClasses) {
        this.classMethods = classMethods;
        this.knownClasses = new LinkedHashSet<>(allClasses);
    }

    /**
     * Generates sequence diagram for a specific entry point
     */
    public void printSequenceDiagram(PrintWriter out, String entryClass, String entryMethod) {
        out.println("sequenceDiagram");
        
        // Find entry point
        ParsedMethod entryPoint = findMethod(entryClass, entryMethod);
        if (entryPoint == null) {
            out.println("    Note over " + entryClass + ": Method '" + entryMethod + "' not found");
            return;
        }

        // Track participants
        Set<String> participants = new LinkedHashSet<>();
        participants.add("Client");
        participants.add(entryClass);

        // Generate sequence
        List<String> sequence = new ArrayList<>();
        Set<String> visited = new LinkedHashSet<>();
        
        // Start the sequence
        sequence.add("Client->>+" + entryClass + ": " + entryMethod + "()");
        
        // Generate calls from the entry method
        generateSequenceFromMethod(entryPoint, entryClass, participants, sequence, visited, 1);
        
        // Return from entry method
        if (!"void".equals(entryPoint.getReturnType())) {
            sequence.add(entryClass + "-->>-Client: " + entryPoint.getReturnType());
        } else {
            sequence.add(entryClass + "-->>-Client: return");
        }

        // Print participants
        for (String participant : participants) {
            out.println("    participant " + participant);
        }
        out.println();

        // Print sequence
        for (String step : sequence) {
            out.println("    " + step);
        }
    }

    /**
     * Generates sequence diagram for all public methods
     */
    public void printAllMethodsSequence(PrintWriter out) {
        out.println("sequenceDiagram");
        
        Set<String> participants = new LinkedHashSet<>();
        participants.add("Client");
        
        List<String> sequence = new ArrayList<>();
        int methodCount = 0;
        
        // Process all public methods as potential entry points
        for (Map.Entry<String, List<ParsedMethod>> entry : classMethods.entrySet()) {
            String className = entry.getKey();
            List<ParsedMethod> methods = entry.getValue();
            
            for (ParsedMethod method : methods) {
                // Only show public methods that are likely to be called externally
                if ("🟢".equals(method.getVisibility()) && 
                    !isGetterSetter(method.getMethodName()) &&
                    !isSystemMethod(method.getMethodName()) &&
                    methodCount < 5) { // Limit to prevent overwhelming diagrams
                    
                    participants.add(className);
                    sequence.add("Client->>+" + className + ": " + method.getMethodName() + "()");
                    
                    Set<String> visited = new LinkedHashSet<>();
                    generateSequenceFromMethod(method, className, participants, sequence, visited, 1);
                    
                    if (!"void".equals(method.getReturnType())) {
                        sequence.add(className + "-->>-Client: " + method.getReturnType());
                    } else {
                        sequence.add(className + "-->>-Client: return");
                    }
                    
                    sequence.add(""); // Empty line for separation
                    methodCount++;
                }
            }
        }

        if (sequence.isEmpty()) {
            sequence.add("Note over Client: No public methods found to display");
        }

        // Print participants
        for (String participant : participants) {
            out.println("    participant " + participant);
        }
        out.println();

        // Print sequence
        for (String step : sequence) {
            if (!step.isEmpty()) {
                out.println("    " + step);
            } else {
                out.println();
            }
        }
    }

    /**
     * Recursively generates sequence steps from a method
     */
    private void generateSequenceFromMethod(ParsedMethod method, String caller, Set<String> participants, 
                                          List<String> sequence, Set<String> visited, int depth) {
        
        // Prevent infinite recursion and overly deep calls
        if (depth > 8) {
            sequence.add("Note over " + caller + ": ... (call depth limit reached)");
            return;
        }
        
        String methodId = method.getClassName() + "." + method.getMethodName();
        if (visited.contains(methodId)) {
            sequence.add("Note over " + caller + ": " + method.getMethodName() + "() (already called)");
            return;
        }
        visited.add(methodId);

        for (MethodCall call : method.getMethodCalls()) {
            String targetClass = call.getTargetClass();
            
            // Skip system classes, primitives, and self-references
            if (isSystemClass(targetClass) || targetClass.equals("this") || targetClass.equals(caller)) {
                continue;
            }
            
            // Resolve "this" calls to current class
            if (targetClass.equals("this")) {
                targetClass = method.getClassName();
            }
            
            // Only include calls to classes we know about
            if (!knownClasses.contains(targetClass)) {
                continue;
            }
            
            participants.add(targetClass);
            
            // Generate appropriate arrow based on call type
            String arrow;
            String methodDisplay;
            
            switch (call.getCallType()) {
                case "constructor":
                    arrow = "->>+";
                    methodDisplay = "new " + call.getMethodName() + "()";
                    sequence.add(caller + arrow + targetClass + ": " + methodDisplay);
                    break;
                    
                case "static":
                    arrow = "->>";
                    methodDisplay = call.getMethodName() + "() [static]";
                    sequence.add(caller + arrow + targetClass + ": " + methodDisplay);
                    break;
                    
                case "method":
                default:
                    arrow = "->>";
                    methodDisplay = call.getMethodName() + "()";
                    sequence.add(caller + arrow + targetClass + ": " + methodDisplay);
                    break;
            }

            // Find the called method and recurse
            ParsedMethod calledMethod = findMethod(targetClass, call.getMethodName());
            if (calledMethod != null && !call.getCallType().equals("constructor")) {
                generateSequenceFromMethod(calledMethod, targetClass, participants, sequence, visited, depth + 1);
                
                // Add return message for non-void methods
                if (!"void".equals(calledMethod.getReturnType())) {
                    sequence.add(targetClass + "-->" + caller + ": " + calledMethod.getReturnType());
                }
            } else if (call.getCallType().equals("constructor")) {
                // For constructors, just show the creation
                sequence.add(targetClass + "-->" + caller + ": new instance");
            }
        }
        
        visited.remove(methodId); // Allow the method to be called again in different contexts
    }

    /**
     * Find a method by class name and method name
     */
    private ParsedMethod findMethod(String className, String methodName) {
        List<ParsedMethod> methods = classMethods.get(className);
        if (methods == null) return null;
        
        return methods.stream()
                .filter(m -> m.getMethodName().equals(methodName))
                .findFirst()
                .orElse(null);
    }

    /**
     * Check if a class name represents a system class that should be filtered out
     */
    private boolean isSystemClass(String className) {
        if (className == null || className.isEmpty()) return true;
        
        return className.startsWith("java.") || 
               className.startsWith("javax.") ||
               className.startsWith("org.springframework.") ||
               className.startsWith("com.fasterxml.") ||
               className.equals("String") ||
               className.equals("Object") ||
               className.equals("System") ||
               className.equals("StringBuilder") ||
               className.equals("StringBuffer") ||
               className.equals("ArrayList") ||
               className.equals("HashMap") ||
               className.equals("LinkedList") ||
               className.equals("HashSet") ||
               className.equals("TreeSet") ||
               className.equals("List") ||
               className.equals("Map") ||
               className.equals("Set") ||
               className.equals("Collection") ||
               className.equals("Optional") ||
               className.equals("Stream") ||
               className.equals("Logger") ||
               className.equals("Exception") ||
               className.equals("RuntimeException");
    }

    /**
     * Check if method name is a getter or setter
     */
    private boolean isGetterSetter(String methodName) {
        return methodName.startsWith("get") || 
               methodName.startsWith("set") || 
               methodName.startsWith("is");
    }

    /**
     * Check if method is a common system method
     */
    private boolean isSystemMethod(String methodName) {
        return methodName.equals("toString") || 
               methodName.equals("hashCode") || 
               methodName.equals("equals") ||
               methodName.equals("clone") ||
               methodName.equals("finalize") ||
               methodName.equals("wait") ||
               methodName.equals("notify") ||
               methodName.equals("notifyAll");
    }
}