package com.codeDiagramerz.web;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.codeDiagramerz.model.ParsedClass;
import com.codeDiagramerz.model.ParsedEnum;
import com.codeDiagramerz.model.ParsedMethod;
import com.codeDiagramerz.output.MermaidDiagramPrinter;
import com.codeDiagramerz.output.SequenceDiagramPrinter;
import com.codeDiagramerz.parser.EnhancedJavaParser;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/DiagramGenerator")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024, //1MB
    maxFileSize = 1024 * 1024 * 10,  //10MB
    maxRequestSize = 1024 * 1024 * 50  //50MB
)
public class DiagramGeneratorServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
	        throws ServletException, IOException {
	    
	    response.setContentType("text/plain;charset=UTF-8");
	    PrintWriter out = response.getWriter();
	    out.flush();	

	    try {
	        List<File> javaFiles = new ArrayList<>();

	        // Handle all uploaded files
	        for (Part part : request.getParts()) {
	            String fileName = part.getSubmittedFileName();
	            if (fileName != null && fileName.endsWith(".java")) 
	            {
	                File tempFile = File.createTempFile("java_", ".java");
	                
	                try (InputStream input = part.getInputStream()) {
	                    Files.copy(input, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
	                }
	                javaFiles.add(tempFile);
	            }
	        }

	        // Handle code from editor
	        String code = request.getParameter("code");
	        if ((javaFiles.isEmpty()) && code != null && !code.trim().isEmpty()) {
	            File tempFile = File.createTempFile("java_", ".java");
	            Files.write(tempFile.toPath(), code.getBytes());
	            javaFiles.add(tempFile);
	        }

	        if (javaFiles.isEmpty()) {
	            out.println("❌ No valid Java files found to process");
	            return;
	        }

	        // Get diagram type parameter
	        String diagramType = request.getParameter("diagramType");
	        if (diagramType == null) {
	            diagramType = "class"; // Default to class diagram
	        }

	        EnhancedJavaParser parser = new EnhancedJavaParser();
	        for (File file : javaFiles) {
	            parser.parsePath(file);
	            file.delete(); // Clean up temp file
	        }

	        Map<String, ParsedClass> classes = parser.getParsedClasses();
	        Map<String, ParsedEnum> enums = parser.getParsedEnums();
	        Map<String, Set<String>> deps = parser.getClassDeps();
	        Map<String, Set<String>> inheritance = parser.getInheritance();
	        Map<String, Set<String>> interfaces = parser.getInterfaces();
	        Map<String, List<ParsedMethod>> methodDetails = parser.getMethodDetails();

	        if ("sequence".equals(diagramType)) {
	            // Generate sequence diagram
	            Set<String> allClasses = new HashSet<>(classes.keySet());
	            allClasses.addAll(enums.keySet());
	            
	            SequenceDiagramPrinter sequencePrinter = new SequenceDiagramPrinter(methodDetails, allClasses);
	            
	            // Check if specific method is requested
	            String entryClass = request.getParameter("entryClass");
	            String entryMethod = request.getParameter("entryMethod");
	            
	            if (entryClass != null && entryMethod != null && !entryClass.trim().isEmpty() && !entryMethod.trim().isEmpty()) {
	                sequencePrinter.printSequenceDiagram(out, entryClass.trim(), entryMethod.trim());
	            } else {
	                // Generate sequence diagram for all public methods
	                sequencePrinter.printAllMethodsSequence(out);
	            }
	        } else {
	            // Generate class diagram (default)
	            MermaidDiagramPrinter classPrinter = new MermaidDiagramPrinter(
	                classes, enums, deps, inheritance, interfaces);
	            classPrinter.printDiagram(out);
	        }

	    } catch (Exception e) {
	        out.println("❌ Error generating diagram: " + e.getMessage());
	        e.printStackTrace(out);
	    }
	}
}







// mark-1

//package com.codeDiagramerz.web;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.PrintWriter;
//import java.nio.file.Files;
//import java.nio.file.StandardCopyOption;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import com.codeDiagramerz.model.ParsedClass;
//import com.codeDiagramerz.model.ParsedEnum;
//import com.codeDiagramerz.output.MermaidDiagramPrinter;
//import com.codeDiagramerz.parser.EnhancedJavaParser;
//
//import jakarta.servlet.ServletException;
//import jakarta.servlet.annotation.MultipartConfig;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.HttpServlet;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import jakarta.servlet.http.Part;
//
//@WebServlet("/DiagramGenerator")
//@MultipartConfig(
//    fileSizeThreshold = 1024 * 1024, //1MB
//    maxFileSize = 1024 * 1024 * 10,  //10MB
//    maxRequestSize = 1024 * 1024 * 50  //50MB
//)
//public class DiagramGeneratorServlet extends HttpServlet {
//
//	@Override
//	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
//	        throws ServletException, IOException {
//	    
//	    response.setContentType("text/plain;charset=UTF-8");
//	    PrintWriter out = response.getWriter();
//	    out.flush();	
//
//	    try {
//	        List<File> javaFiles = new ArrayList<>();
////	        System.out.println("At Start: "+javaFiles);    //for checking tempIfiles 
//
//	        // Handle all uploaded files
//	        for (Part part : request.getParts()) {
//	            String fileName = part.getSubmittedFileName();
//	            if (fileName != null && fileName.endsWith(".java")) 
//	            {
//
//	                File tempFile = File.createTempFile("java_", ".java");
//	                
//	                try (InputStream input = part.getInputStream()) {
//	                    Files.copy(input, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
//	                }
//	                javaFiles.add(tempFile);
//
//	            	
//	            }
//	        }
//
//	        // Handle code from editor
//	        String code = request.getParameter("code");
//	        if ((javaFiles.isEmpty()) && code != null && !code.trim().isEmpty()) {
//	            File tempFile = File.createTempFile("java_", ".java");
//	            Files.write(tempFile.toPath(), code.getBytes());
//	            javaFiles.add(tempFile);
//	        }
//
//	        if (javaFiles.isEmpty()) {
//	            out.println("❌ No valid Java files found to process");
//	            return;
//	        }
//
//	        EnhancedJavaParser parser = new EnhancedJavaParser();
//	        for (File file : javaFiles) {
//	            parser.parsePath(file);
//	            file.delete(); // Clean up temp file
//	        }
//
//
//	        Map<String, ParsedClass> classes = parser.getParsedClasses();
//	        Map<String, ParsedEnum> enums = parser.getParsedEnums();
//	        Map<String, Set<String>> deps = parser.getClassDeps();
//	        Map<String, Set<String>> inheritance = parser.getInheritance();
//	        Map<String, Set<String>> interfaces = parser.getInterfaces();
//
//	        MermaidDiagramPrinter printer = new MermaidDiagramPrinter(
//	            classes, enums, deps, inheritance, interfaces);
//	        printer.printDiagram(out);
////	        System.out.println("At End: "+javaFiles);
////	        javaFiles = null;
////	        System.out.println("At End2: "+javaFiles);
//
//	    } catch (Exception e) {
//	        out.println("❌ Error generating diagram: " + e.getMessage());
//	        e.printStackTrace(out);
//	    }
//	}
//
//}

