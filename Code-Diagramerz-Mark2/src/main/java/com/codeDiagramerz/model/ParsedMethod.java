package com.codeDiagramerz.model;

import java.util.ArrayList;
import java.util.List;

public class ParsedMethod {
    private final String className;
    private final String methodName;
    private final String returnType;
    private final List<String> parameters;
    private final List<MethodCall> methodCalls;
    private final boolean isConstructor;
    private final String visibility;

    public ParsedMethod(String className, String methodName, String returnType, 
                       List<String> parameters, String visibility, boolean isConstructor) {
        this.className = className;
        this.methodName = methodName;
        this.returnType = returnType;
        this.parameters = new ArrayList<>(parameters);
        this.methodCalls = new ArrayList<>();
        this.isConstructor = isConstructor;
        this.visibility = visibility;
    }

    public void addMethodCall(MethodCall call) {
        methodCalls.add(call);
    }

    // Getters
    public String getClassName() { return className; }
    public String getMethodName() { return methodName; }
    public String getReturnType() { return returnType; }
    public List<String> getParameters() { return parameters; }
    public List<MethodCall> getMethodCalls() { return methodCalls; }
    public boolean isConstructor() { return isConstructor; }
    public String getVisibility() { return visibility; }

    public static class MethodCall {
        private final String targetClass;
        private final String methodName;
        private final String callType; // "method", "constructor", "static"
        private final int lineNumber;

        public MethodCall(String targetClass, String methodName, String callType, int lineNumber) {
            this.targetClass = targetClass;
            this.methodName = methodName;
            this.callType = callType;
            this.lineNumber = lineNumber;
        }

        // Getters
        public String getTargetClass() { return targetClass; }
        public String getMethodName() { return methodName; }
        public String getCallType() { return callType; }
        public int getLineNumber() { return lineNumber; }
    }
}