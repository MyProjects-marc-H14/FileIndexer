package com.marchofer.fileindexer;

import java.io.*;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class CSVFileIndexer {
    private static FileWriter pw;
    private static boolean debug = false;
    private static boolean debugExtended = false;
    private static boolean size = true;
    private static boolean userProfile = false;
    private static boolean noConfigDirs = false;
    private static boolean useIndexPaths = false;
    private static final ArrayList<Path> indexPaths = new ArrayList<>();
    private static int folderNumber;
    private static int fileNumber;

    public static void main(String[] args) {
        try {
            if (Arrays.asList(args).contains("-h")) {
                System.out.println("FileIndexer 1.0 CSV options");
                System.out.println("Usage: java -jar FileIndexer-1.0-CSV.jar [options] [parameters]");
                System.out.println("options: ");
                System.out.println("-d:  prints debug messages");
                System.out.println("-dx: prints extended debug messages");
                System.out.println("-u:  index only %userprofile%");
                System.out.println("-a:  excludes config dirs and AppData from index");
                System.out.println("-s:  excludes size from output");
                System.out.println("parameters");
                System.out.println("-i /path/to/index: index certain path, may be used multiple times");
                System.exit(0);
            }
            for (int i = 0; i < args.length; i++) {
                if (i > 0 && args[i - 1].equals("-i")) continue;
                switch (args[i]) {
                    case "-u":
                        userProfile = true;
                        break;
                    case "-a":
                        noConfigDirs = true;
                        break;
                    case "-d":
                        debug = true;
                        break;
                    case "-dx":
                        debug = true;
                        debugExtended = true;
                        break;
                    case "s":
                        size = true;
                        break;
                    case "-i":
                        if (args.length <= i + 1) {
                            continue;
                        }
                        useIndexPaths = true;
                        indexPaths.add(Paths.get(args[i + 1]));
                        break;
                }
            }
            System.out.println("FileIndexer 1.0 started");
            System.out.println("Starting indexing, saving to index.csv");
            long start = System.currentTimeMillis();
            String csvFileName = "index.csv";
            pw = new FileWriter(csvFileName);
            pw.append("path");
            if (size) pw.append(";size");
            //pw.append(";read;write;execute\n");
            StringBuilder sb = new StringBuilder();
            sb.append("DRIVE || FILES        FOLDERS      TOTAL        TIME(ms)\n");
            sb.append("======||================================================\n");
            long driveStart;
            int totalFileNumber = 0;
            int totalFolderNumber = 0;
            if (!userProfile) {
                for (File root: File.listRoots()) {
                    driveStart = System.currentTimeMillis();
                    fileNumber = 0;
                    folderNumber = 0;
                    Path path = Paths.get(root.toURI());
                    index(path);
                    sb.append(String.format("%-5s || %-11d  %-11d  %-11d  %-20d\n", root.toString(),
                            fileNumber, folderNumber, fileNumber + folderNumber, System.currentTimeMillis() - driveStart));
                    totalFileNumber += fileNumber;
                    totalFolderNumber += folderNumber;
                }
            } else {
                driveStart = System.currentTimeMillis();
                fileNumber = 0;
                folderNumber = 0;
                Path path = Paths.get(System.getProperty("user.home"));
                index(path);
                sb.append(String.format("%-5s || %-11d  %-11d  %-11d  %-20d\n", "USER",
                        fileNumber, folderNumber, fileNumber + folderNumber, System.currentTimeMillis() - driveStart));
                totalFileNumber += fileNumber;
                totalFolderNumber += folderNumber;
            }
            if (useIndexPaths) {
                ArrayList<Path> remaining = new ArrayList<>(indexPaths);
                int i = 0;
                for (Path path: remaining) {
                    driveStart = System.currentTimeMillis();
                    fileNumber = 0;
                    folderNumber = 0;
                    index(path);
                    sb.append(String.format("%-5s || %-11d  %-11d  %-11d  %-20d\n", "I" + i,
                            fileNumber, folderNumber, fileNumber + folderNumber, System.currentTimeMillis() - driveStart));
                    totalFileNumber += fileNumber;
                    totalFolderNumber += folderNumber;
                    i++;
                }
            }
            sb.append("------||------------------------------------------------\n");
            sb.append(String.format("%-5s || %-11d  %-11d  %-11d  %-20d\n", "TOTAL",
                    totalFileNumber, totalFolderNumber, totalFileNumber + totalFolderNumber,
                    System.currentTimeMillis() - start));
            System.out.println("Done");
            System.out.println(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void index(Path path) {
        if (debug && path.getNameCount() < 3 && !debugExtended) System.out.println(path);
        if (debugExtended) System.out.println(path);
        if (noConfigDirs && (path.getFileName().toString().equals("AppData") ||
                path.getFileName().toString().startsWith(".")) &&
                !(useIndexPaths && indexPaths.contains(path))) return;
        if (useIndexPaths) indexPaths.remove(path);
        try {
            Files.list(path).forEach(filePath -> {
                long size = -1;
                if (CSVFileIndexer.size) {
                    try {
                        size = Files.size(filePath);
                    } catch (IOException e) {
                        if (debug) System.err.println(e.getMessage());
                    }
                }
                //System.out.println(System.currentTimeMillis() - start);
                try {
                    pw.append(filePath.toString());
                    if (CSVFileIndexer.size) {
                        pw.append(";");
                        pw.append(String.valueOf(size));
                    }
                    /*pw.append(";");
                    pw.append(String.valueOf(Files.isReadable(filePath)));
                    pw.append(";");
                    pw.append(String.valueOf(Files.isWritable(filePath)));
                    pw.append(";");
                    pw.append(String.valueOf(Files.isExecutable(filePath)));*/
                    pw.append("\n");
                } catch (IOException e) {
                    if (debug) System.err.println(e.getMessage());
                }
                if (Files.isDirectory(filePath)) {
                    index(filePath);
                    folderNumber++;
                } else {
                    fileNumber++;
                }
            });
        } catch (AccessDeniedException e) {
            if (debug) System.err.println("Access Denied: " + path);
        } catch (IOException e) {
            if (debug) System.err.println(e.getMessage());
        }
    }
}
