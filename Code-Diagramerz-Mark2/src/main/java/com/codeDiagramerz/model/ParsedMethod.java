package com.codeDiagramerz.model;

import java.util.ArrayList;
import java.util.List;

public class ParsedMethod {
    private final String className;
    private final String methodName;
    private final String returnType;
    private final List<String> parameters;
    private final List<MethodCall> methodCalls;
    private final List<FlowPattern> flowPatterns;
    private final boolean isConstructor;
    private final String visibility;
    private String methodBody;  // Store method body for pattern analysis

    public ParsedMethod(String className, String methodName, String returnType, 
                       List<String> parameters, String visibility, boolean isConstructor) {
        this.className = className;
        this.methodName = methodName;
        this.returnType = returnType;
        this.parameters = new ArrayList<>(parameters);
        this.methodCalls = new ArrayList<>();
        this.flowPatterns = new ArrayList<>();
        this.isConstructor = isConstructor;
        this.visibility = visibility;
    }

    public void addMethodCall(MethodCall call) {
        methodCalls.add(call);
    }

    public void addFlowPattern(FlowPattern pattern) {
        flowPatterns.add(pattern);
    }

    public void setMethodBody(String methodBody) {
        this.methodBody = methodBody;
        analyzeFlowPatterns();
    }

    private void analyzeFlowPatterns() {
        if (methodBody == null) return;

        // Detect threading patterns
        if (methodBody.contains("new Thread") && methodBody.contains(".start()")) {
            flowPatterns.add(new FlowPattern("THREAD_CREATION", "Creates and starts threads"));
        }
        
        if (methodBody.contains("thread.join()") || methodBody.contains(".join()")) {
            flowPatterns.add(new FlowPattern("THREAD_JOIN", "Waits for threads to complete"));
        }

        // Detect loop patterns
        if (methodBody.contains("for (") || methodBody.contains("for(")) {
            flowPatterns.add(new FlowPattern("FOR_LOOP", "Contains for loop"));
        }

        // Detect conditional patterns
        if (methodBody.contains("Math.random()") || methodBody.contains("if (")) {
            flowPatterns.add(new FlowPattern("CONDITIONAL", "Contains conditional logic"));
        }

        // Detect array/collection operations
        if (methodBody.contains("new ") && methodBody.contains("[]")) {
            flowPatterns.add(new FlowPattern("ARRAY_CREATION", "Creates arrays"));
        }

        // Detect synchronization
        if (methodBody.contains("synchronized") || visibility.contains("synchronized")) {
            flowPatterns.add(new FlowPattern("SYNCHRONIZED", "Synchronized method"));
        }

        // Detect system interactions
        if (methodBody.contains("System.out.println") || methodBody.contains("println")) {
            flowPatterns.add(new FlowPattern("SYSTEM_OUTPUT", "Prints to console"));
        }
        
        if (methodBody.contains("Thread.sleep")) {
            flowPatterns.add(new FlowPattern("THREAD_SLEEP", "Thread sleeps"));
        }
    }

    // Getters
    public String getClassName() { return className; }
    public String getMethodName() { return methodName; }
    public String getReturnType() { return returnType; }
    public List<String> getParameters() { return parameters; }
    public List<MethodCall> getMethodCalls() { return methodCalls; }
    public List<FlowPattern> getFlowPatterns() { return flowPatterns; }
    public boolean isConstructor() { return isConstructor; }
    public String getVisibility() { return visibility; }
    public String getMethodBody() { return methodBody; }

    public boolean hasPattern(String patternType) {
        return flowPatterns.stream().anyMatch(p -> p.getType().equals(patternType));
    }

    public static class MethodCall {
        private final String targetClass;
        private final String methodName;
        private final String callType;
        private final int lineNumber;
        private final String context; // Additional context like "in loop", "conditional", etc.

        public MethodCall(String targetClass, String methodName, String callType, int lineNumber) {
            this(targetClass, methodName, callType, lineNumber, "");
        }

        public MethodCall(String targetClass, String methodName, String callType, int lineNumber, String context) {
            this.targetClass = targetClass;
            this.methodName = methodName;
            this.callType = callType;
            this.lineNumber = lineNumber;
            this.context = context;
        }

        // Getters
        public String getTargetClass() { return targetClass; }
        public String getMethodName() { return methodName; }
        public String getCallType() { return callType; }
        public int getLineNumber() { return lineNumber; }
        public String getContext() { return context; }
    }

    public static class FlowPattern {
        private final String type;
        private final String description;

        public FlowPattern(String type, String description) {
            this.type = type;
            this.description = description;
        }

        public String getType() { return type; }
        public String getDescription() { return description; }
    }
}