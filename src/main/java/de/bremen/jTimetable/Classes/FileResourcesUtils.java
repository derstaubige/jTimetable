package de.bremen.jTimetable.Classes;

// From https://mkyong.com/java/java-read-a-file-from-resources-folder/
// package com.mkyong.io.utils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FileResourcesUtils {

    public static List<Path> start() throws IOException {

        FileResourcesUtils app = new FileResourcesUtils();
        String jarorFile = FileResourcesUtils.class.getResource("FileResourcesUtils.class").getProtocol();
        
        List<Path> result = new ArrayList<Path>();
        try {
            
            if(jarorFile == "file"){
                Path currentWorkingDir = Paths.get("","Target","classes","SQLMigration").toAbsolutePath();
       
                result = app.getPathsFromFile(currentWorkingDir.normalize().toString());
            }else{
                result = app.getPathsFromResourceJAR("SQLMigration");
            }
            
            

        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    // get a file from the resources folder
    // works everywhere, IDEA, unit test and JAR file.
    public InputStream getFileFromResourceAsStream(String fileName) {

        // The class loader that loaded the class
        // ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = getClass().getResourceAsStream(fileName);

        // the stream holding the file content
        if (inputStream == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return inputStream;
        }

    }

    private List<Path> getPathsFromFile(String folderpath){
        File folder = new File(folderpath);
        File[] listOfFiles = folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String name = pathname.getAbsolutePath();
                return name.toLowerCase().endsWith(".sql");
            }
        });
        List<Path> returnPathlist = new ArrayList<Path>();
        for (int i = 0; i < listOfFiles.length; i++) {
            returnPathlist.add(listOfFiles[i].toPath());
        }
        Collections.sort(returnPathlist);
        return returnPathlist;
    }

    // Get all paths from a folder that inside the JAR file
    private List<Path> getPathsFromResourceJAR(String folder)
        throws URISyntaxException, IOException {

        List<Path> result;

        // get path of the current running JAR
        URI jarPath = getClass().getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI();
        System.out.println("JAR Path : " + jarPath);

        // file walks JAR
        URI uri = URI.create("jar:" + jarPath);
        try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
            result = Files.walk(fs.getPath(folder))
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
        }
        Collections.sort(result);
        return result;

    }
}