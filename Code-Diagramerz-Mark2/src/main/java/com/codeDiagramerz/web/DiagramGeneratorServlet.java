package com.codeDiagramerz.web;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.codeDiagramerz.model.ParsedClass;
import com.codeDiagramerz.model.ParsedEnum;
import com.codeDiagramerz.output.MermaidDiagramPrinter;
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
	        System.out.println("At Start: "+javaFiles);

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
	            tempFile.delete();
	        }

	        if (javaFiles.isEmpty()) {
	            out.println("❌ No valid Java files found to process");
	            return;
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

	        MermaidDiagramPrinter printer = new MermaidDiagramPrinter(
	            classes, enums, deps, inheritance, interfaces);
	        printer.printDiagram(out);
	        System.out.println("At End: "+javaFiles);
	        javaFiles = null;
	        System.out.println("At End2: "+javaFiles);

	    } catch (Exception e) {
	        out.println("❌ Error generating diagram: " + e.getMessage());
	        e.printStackTrace(out);
	    }
	}

}










// Mark2.0 =>

//@Override
//protected void doPost(HttpServletRequest request, HttpServletResponse response) 
//      throws ServletException, IOException {
//  
//  response.setContentType("text/plain;charset=UTF-8");
//  PrintWriter out = response.getWriter();
//
//  try {
//      List<File> javaFiles = new ArrayList<>();
//
//      // Handle all uploaded files (both from Java files and folder upload)
//      for (Part part : request.getParts()) {
//          if (part.getSubmittedFileName() != null && part.getSubmittedFileName().endsWith(".java")) {
//              File tempFile = File.createTempFile("java_", ".java");
//              
//              try (InputStream input = part.getInputStream()) {
//                  Files.copy(input, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
//              }
//              javaFiles.add(tempFile);
//          }
//      }
//      
//      
//      
//      
//      
//
//      if (javaFiles.isEmpty()) {
//          out.println("❌ No valid Java files found to process");
//          return;
//      }
//
//      EnhancedJavaParser parser = new EnhancedJavaParser();
//      for (File file : javaFiles) {
//          parser.parsePath(file);
//          file.delete();
//      }
//
//      Map<String, ParsedClass> classes = parser.getParsedClasses();
//      Map<String, ParsedEnum> enums = parser.getParsedEnums();
//      Map<String, Set<String>> deps = parser.getClassDeps();
//      Map<String, Set<String>> inheritance = parser.getInheritance();
//      Map<String, Set<String>> interfaces = parser.getInterfaces();
//
//      MermaidDiagramPrinter printer = new MermaidDiagramPrinter(
//          classes, enums, deps, inheritance, interfaces);
//      printer.printDiagram(out);
//
//  } catch (Exception e) {
//      out.println("❌ Error generating diagram: " + e.getMessage());
//      e.printStackTrace(out);
//  }
//}

    
    
    
    
    
    
    
    
//    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
//            throws ServletException, IOException {
//        
//        response.setContentType("text/plain");
//        PrintWriter out = response.getWriter();
//
//        try {
//            String code = request.getParameter("code");
//            List<File> javaFiles = new ArrayList<>();
//
//            if (code != null && !code.trim().isEmpty()) {
//                // Handle code from text editor
//                File tempFile = File.createTempFile("java_", ".java");
//                Files.write(tempFile.toPath(), code.getBytes());
//                javaFiles.add(tempFile);
//            } else {
//                // Handle file uploads
//                for (Part part : request.getParts()) {
//                    if (part.getSubmittedFileName() != null) {
//                        String fileName = part.getSubmittedFileName();
//                        
//                        if (fileName.endsWith(".zip")) {
//                            extractJavaFilesFromZip(part.getInputStream(), javaFiles);
//                        } else if (fileName.endsWith(".java")) {
//                            File tempFile = File.createTempFile("java_", ".java");
//                            try (InputStream input = part.getInputStream()) {
//                                Files.copy(input, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
//                            }
//                            javaFiles.add(tempFile);
//                        }
//                    }
//                }
//            }
//
//            if (javaFiles.isEmpty()) {
//                out.println("❌ No Java files or code provided.");
//                return;
//            }
//
//            EnhancedJavaParser parser = new EnhancedJavaParser();
//            for (File file : javaFiles) {
//                parser.parsePath(file);
//                file.delete();
//            }
//
//            Map<String, ParsedClass> classes = parser.getParsedClasses();
//            Map<String, ParsedEnum> enums = parser.getParsedEnums();
//            Map<String, Set<String>> deps = parser.getClassDeps();
//            Map<String, Set<String>> inheritance = parser.getInheritance();
//            Map<String, Set<String>> interfaces = parser.getInterfaces();
//
//            MermaidDiagramPrinter printer = new MermaidDiagramPrinter(
//                classes, enums, deps, inheritance, interfaces);
//            printer.printDiagram(out);
//
//        } catch (Exception e) {
//            out.println("❌ Error generating diagram: " + e.getMessage());
//            e.printStackTrace(out);
//        }
//    }

//    private void extractJavaFilesFromZip(InputStream zipStream, List<File> javaFiles) 
//            throws IOException {
//        Path tempDir = Files.createTempDirectory("java_files_");
//        try (ZipInputStream zis = new ZipInputStream(zipStream)) {
//            ZipEntry entry;
//            while ((entry = zis.getNextEntry()) != null) {
//                if (!entry.isDirectory() && entry.getName().endsWith(".java")) {
//                    File tempFile = tempDir.resolve(entry.getName()).toFile();
//                    tempFile.getParentFile().mkdirs();
//                    Files.copy(zis, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
//                    javaFiles.add(tempFile);
//                }
//            }
//        }
//    }
