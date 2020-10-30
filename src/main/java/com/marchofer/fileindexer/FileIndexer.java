package com.marchofer.fileindexer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

public class FileIndexer {
    /*private static String PERMS = "p";
    private static String TYPE = "t";
    private static String SIZE = "s";
    private static String CREATED = "g";
    private static String MODIFY = "m";
    private static String ACCESS = "a";
    private static String CONTENT = "c";

    private static boolean ct = false;
    private static boolean mt = false;
    private static boolean lt = false;
    private static boolean t = false;
    private static boolean s = true;
    private static boolean d = true;

    public static void main(String[] args) throws IOException {
        if (Arrays.asList(args).contains("-h")) {
            System.out.println("Usage: java -jar com.marchofer.fileindexer.FileIndexer.jar [options]");
            System.out.println("Options:");
            System.out.println("-d | --no-debug: don't print debug info");
            System.out.println("                -> current dir if < 3 folder levels and access denied messages");
            System.out.println("-l | --longKeys: use long key names in json");
            System.out.println("-c | --creationTime: include file creation time in json");
            System.out.println("-m | --modificationTime: include file modification time in json");
            System.out.println("-la | --lastAccessTime: include file last access time in json");
            System.out.println("-t | --type: include file type in json");
            System.out.println("-s | --no-size: don't include file size in json");
            System.exit(0);
        }
        if (Arrays.asList(args).contains("--longKeys") || Arrays.asList(args).contains("-l")) {
            PERMS = "perms";
            TYPE = "type";
            SIZE = "size";
            CREATED = "create";
            MODIFY = "modify";
            ACCESS = "access";
            CONTENT = "content";
        }
        if (Arrays.asList(args).contains("--creationTime") || Arrays.asList(args).contains("-c")) {
            ct = true;
        }
        if (Arrays.asList(args).contains("--modificationTime") || Arrays.asList(args).contains("-m")) {
            mt = true;
        }
        if (Arrays.asList(args).contains("--lastAccessTime") || Arrays.asList(args).contains("-la")) {
            lt = true;
        }
        if (Arrays.asList(args).contains("--type") || Arrays.asList(args).contains("-t")) {
            t = true;
        }
        if (Arrays.asList(args).contains("--no-size") || Arrays.asList(args).contains("-s")) {
            s = false;
        }
        if (Arrays.asList(args).contains("--no-debug") || Arrays.asList(args).contains("-d")) {
            d = false;
        }
        long start = System.currentTimeMillis();
        JSONObject filesObject = new JSONObject();
        System.out.println("Starting indexing");
        for (File root: File.listRoots()) {
            //if (root.equals(new File("A:\\"))) {
            Path path = Paths.get(root.toURI());
            filesObject.put(path.toString().replace("\\", ""), index(path));
                //System.out.println(filesObject.toString(1));
            //}
        }
        System.out.println("Done indexing");
        FileWriter file = new FileWriter("index.json");
        file.write(filesObject.toString(1));
        file.close();
        System.out.println("Saved as index.json");
        System.out.println("Time: " + ((double)(System.currentTimeMillis() - start)/1000) + "s");
    }

    private static JSONObject index(Path path) {
        JSONObject files = getFileAttributes(path);
        JSONObject content = new JSONObject();
        if (d && path.getNameCount() < 3) System.out.println(path);
        try {
            Files.list(path).forEach(filePath -> {
                if (Files.isDirectory(filePath)) {
                    content.put(filePath.getFileName().toString(), index(filePath));
                } else {
                    content.put(filePath.getFileName().toString(), getFileAttributes(filePath));
                }
            });
        } catch (AccessDeniedException e) {
            if (d) System.err.println("Access Denied: " + path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        files.put(CONTENT, content);
        return files;
    }

    private static JSONObject getFileAttributes(Path path) {
        JSONObject files = new JSONObject();
        //files.put("name", path.getFileName());
        int perms = 0;
        if (Files.isExecutable(path)) perms += 1;
        if (Files.isWritable(path)) perms += 2;
        if (Files.isReadable(path)) perms += 4;
        try {
            if (Files.isHidden(path)) perms += 8;
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Files.isDirectory(path)) perms += 16;
        if (Files.isSymbolicLink(path)) perms += 32;
        files.put(PERMS, perms);
        try {
            if (t) files.put(TYPE, Files.probeContentType(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (s) files.put(SIZE, Files.size(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        BasicFileAttributes attr = null;
        try {
            attr = Files.readAttributes(path, BasicFileAttributes.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (ct) files.putOpt(CREATED, attr.creationTime());
        if (lt) files.putOpt(ACCESS, attr.lastAccessTime());
        if (mt) files.putOpt(MODIFY, attr.lastModifiedTime());
        return files;
    }*/
}
