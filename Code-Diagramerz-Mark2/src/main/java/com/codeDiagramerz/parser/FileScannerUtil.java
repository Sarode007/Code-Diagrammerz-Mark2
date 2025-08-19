package com.codeDiagramerz.parser;


import java.io.File;
import java.util.List;

public class FileScannerUtil {
    public static void findAllJavaFiles(File dir, List<File> javaFiles) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                findAllJavaFiles(f, javaFiles);
            } else if (f.getName().endsWith(".java")) {
                javaFiles.add(f);
            }
        }
    }
}
