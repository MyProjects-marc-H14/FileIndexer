package com.marchofer.fileindexer;




import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.*;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

public class Test {
    private static int fileNumber;
    private static int folderNumber;

    private static boolean size;
    private static boolean perms;
    private static String pathString = "C:/Users/Marc Hofer/Downloads/index.csv";
    private static Path path = Paths.get(pathString);
    private static File file = new File(pathString);
    private static String pathStringIndex = "C:/Users/Marc Hofer/Downloads/";
    private static Path pathIndex = Paths.get(pathStringIndex);
    private static File fileIndex = new File(pathStringIndex);

    public static void main(String[] args) throws IOException {
        /*System.out.println("DRIVE || FILES        FOLDERS      TOTAL        TIME(ms)");
        System.out.println("======||================================================");
        System.out.println(String.format("%-5s || %-11d  %-11d  %-11d  %-20d", "A:\\", 3805432, 4389, 2346, 2634));
        System.out.println("------||------------------------------------------------");
        System.out.println(String.format("%-5s || %-11d  %-11d  %-11d  %-20d", "TOTAL", 3805432, 4389, 2346, 2634));

        Directory directory = FSDirectory.open(Paths.get("C:/Users/Marc Hofer/Downloads/Lucene"));*/
        /*System.out.println("RUN    FILES        FOLDERS      TOTAL        TIME(ms)");
        System.out.println("======================================================");
        size = true;
        perms = false;
        fileNumber = 0;
        folderNumber = 0;
        long start = System.currentTimeMillis();
        index(Paths.get("C:/Users/Marc Hofer/Downloads"));
        long time = System.currentTimeMillis() - start;
        System.out.println(String.format("%-5s  %-11d  %-11d  %-11d  %-20d",
                "SIZE", fileNumber, folderNumber, fileNumber + folderNumber, time));
        size = false;
        perms = true;
        fileNumber = 0;
        folderNumber = 0;
        start = System.currentTimeMillis();
        index(Paths.get("C:/Users/Marc Hofer/Downloads"));
        time = System.currentTimeMillis() - start;
        System.out.println(String.format("%-5s  %-11d  %-11d  %-11d  %-20d",
                "PERMS", fileNumber, folderNumber, fileNumber + folderNumber, time));
        /*size = true;
        fileNumber = 0;
        folderNumber = 0;
        start = System.currentTimeMillis();
        index(Paths.get("C:/Users/Marc Hofer/Downloads"));
        time = System.currentTimeMillis() - start;
        System.out.println(String.format("%-5s  %-11d  %-11d  %-11d  %-20d",
                "BOTH", fileNumber, folderNumber, fileNumber + folderNumber, time));
        size = false;
        perms = false;
        fileNumber = 0;
        folderNumber = 0;
        start = System.currentTimeMillis();
        index(Paths.get("C:/Users/Marc Hofer/Downloads"));
        time = System.currentTimeMillis() - start;
        System.out.println(String.format("%-5s  %-11d  %-11d  %-11d  %-20d",
                "NONE", fileNumber, folderNumber, fileNumber + folderNumber, time));*/
        long start = 0;
        boolean read = false;
        boolean write = false;
        boolean execute = false;
        boolean directory = false;
        long size = 0;
        long timeNIO = 0;
        DosFileAttributes fileAttr = null;
        for (int i = 0; i < 10000; i++) {
            start = System.currentTimeMillis();
            try {
                //attr = Files.readAttributes(path, BasicFileAttributes.class);
                //map = Files.readAttributes(path, "*");
                fileAttr = Files.readAttributes(path, DosFileAttributes.class);
                directory = fileAttr.isDirectory();
                size = fileAttr.size();
            } catch (/*IO*/Exception e) {
                e.printStackTrace();
            }
            timeNIO += System.currentTimeMillis() - start;
        }
        System.out.println("\nBulkAttr (x10000)");
        System.out.print("NIO:  " + fileAttr + ": ");
        System.out.println(timeNIO);
        /*for (AclEntry entry: acl.getAcl()) {
            System.out.println(entry.permissions());
            System.out.println(entry.type());
            System.out.println(entry.principal());
        }*/

        read();
        write();
        execute();
        directory();
        size();
        index();
    }

    private static void read() {
        long start = 0;
        boolean test = false;
        boolean test2 = false;
        long timeNIO = 0;
        long timeFile = 0;

        for (int i = 0; i < 10000; i++) {
            start = System.currentTimeMillis();
            test = file.canRead();
            timeFile += System.currentTimeMillis() - start;
        }
        for (int i = 0; i < 10000; i++) {
            start = System.currentTimeMillis();
            test2 = Files.isReadable(path);
            timeNIO += System.currentTimeMillis() - start;
        }
        System.out.println("\nIsReadable (x10000)");
        System.out.print("NIO:  " + String.valueOf(test) + ": ");
        System.out.println(timeNIO);
        System.out.print("File: " + String.valueOf(test2) + ": ");
        System.out.println(timeFile);
    }

    private static void write() {
        long start = 0;
        boolean test = false;
        boolean test2 = false;
        long timeNIO = 0;
        long timeFile = 0;

        for (int i = 0; i < 10000; i++) {
            start = System.currentTimeMillis();
            test = file.canWrite();
            timeFile += System.currentTimeMillis() - start;
        }
        for (int i = 0; i < 10000; i++) {
            start = System.currentTimeMillis();
            test2 = Files.isWritable(path);
            timeNIO += System.currentTimeMillis() - start;
        }
        System.out.println("\nIsWritable (x10000)");
        System.out.print("NIO:  " + String.valueOf(test) + ": ");
        System.out.println(timeNIO);
        System.out.print("File: " + String.valueOf(test2) + ": ");
        System.out.println(timeFile);
    }

    private static void execute() {
        long start = 0;
        boolean test = false;
        boolean test2 = false;
        long timeNIO = 0;
        long timeFile = 0;

        for (int i = 0; i < 10000; i++) {
            start = System.currentTimeMillis();
            test = file.canExecute();
            timeFile += System.currentTimeMillis() - start;
        }
        for (int i = 0; i < 10000; i++) {
            start = System.currentTimeMillis();
            test2 = Files.isExecutable(path);
            timeNIO += System.currentTimeMillis() - start;
        }
        System.out.println("\nIsExecutable (x10000)");
        System.out.print("NIO:  " + String.valueOf(test) + ": ");
        System.out.println(timeNIO);
        System.out.print("File: " + String.valueOf(test2) + ": ");
        System.out.println(timeFile);
    }

    private static void directory() {
        long start = 0;
        boolean test = false;
        boolean test2 = false;
        long timeNIO = 0;
        long timeFile = 0;

        for (int i = 0; i < 10000; i++) {
            start = System.currentTimeMillis();
            test = file.isDirectory();
            timeFile += System.currentTimeMillis() - start;
        }
        for (int i = 0; i < 10000; i++) {
            start = System.currentTimeMillis();
            test2 = Files.isDirectory(path);
            timeNIO += System.currentTimeMillis() - start;
        }
        System.out.println("\nIsDirectory (x10000)");
        System.out.print("NIO:  " + String.valueOf(test) + ": ");
        System.out.println(timeNIO);
        System.out.print("File: " + String.valueOf(test2) + ": ");
        System.out.println(timeFile);
    }

    private static void size() {
        long start = 0;
        long test = 0;
        long test2 = 0;
        long timeNIO = 0;
        long timeFile = 0;

        for (int i = 0; i < 10000; i++) {
            start = System.currentTimeMillis();
            test = file.length();
            timeFile += System.currentTimeMillis() - start;
        }
        for (int i = 0; i < 10000; i++) {
            start = System.currentTimeMillis();
            try {
                test2 = Files.size(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
            timeNIO += System.currentTimeMillis() - start;
        }
        System.out.println("\nSize (x10000)");
        System.out.print("NIO:  " + String.valueOf(test) + ": ");
        System.out.println(timeNIO);
        System.out.print("File: " + String.valueOf(test2) + ": ");
        System.out.println(timeFile);
    }

    private static void index() {
        long start = 0;
        long timeNIO = 0;
        long timeFile = 0;
        for (int i = 0; i < 50; i++) {
            start = System.currentTimeMillis();
            index(pathIndex);
            timeFile += System.currentTimeMillis() - start;
        }
        for (int i = 0; i < 50; i++) {
            start = System.currentTimeMillis();
            index(fileIndex);
            timeNIO += System.currentTimeMillis() - start;
        }
        System.out.println("\nIndex dir (x100)");
        System.out.print("NIO:  ");
        System.out.println(timeNIO);
        System.out.print("File: ");
        System.out.println(timeFile);
    }

    private static void index(File dir) {
        for (File file: dir.listFiles()) {
            if (file.isDirectory()) {
                index(file);
            }
        }
    }

    private static void index(Path dir) {
        try {
            Files.list(dir).forEach((file) -> {
                if (Files.isDirectory(file)) {
                    index(file);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
