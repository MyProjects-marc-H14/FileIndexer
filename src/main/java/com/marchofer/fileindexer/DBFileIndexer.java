package com.marchofer.fileindexer;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.DosFileAttributes;
import java.sql.*;
import java.util.Arrays;

public class DBFileIndexer {
    private static boolean debug = false;
    private static boolean size = true;

    private static Connection connection;
    private static Statement statement;
    private static PreparedStatement preparedStatement;

    private static int folderNumber;
    private static int fileNumber;
    private static int totalFolderNumber;
    private static int totalFileNumber;

    private static long appStart = System.currentTimeMillis();
    public static void main(String[] args) {
        if (Arrays.asList(args).contains("-h")) {
            System.out.println("FileIndexer CSV options");
            System.out.println("-d: prints debug messages");
            System.out.println("-s: excludes size from output");
            System.exit(0);
        }
        if (Arrays.asList(args).contains("-d")) debug = true;
        if (Arrays.asList(args).contains("-s")) size = false;
        try {
            long start = System.currentTimeMillis();
            Class.forName("org.h2.Driver");
            System.out.println("Connecting to database");
            connection = DriverManager.getConnection("jdbc:h2:./index");
            statement = connection.createStatement();
            String sql;
            sql = "DROP TABLE IF EXISTS FileIndex; " +
                    "CREATE TABLE FileIndex (" +
                    " path TEXT, " +
                    " size BIGINT );";
            if (!size) sql = "DROP TABLE IF EXISTS FileIndex; " +
                    "CREATE TABLE FileIndex (" +
                    " path TEXT );";
            statement.executeUpdate(sql);
            if (size) {
                preparedStatement = connection.prepareStatement("INSERT INTO FILEINDEX VALUES ( ?, ?)");
            } else {
                preparedStatement = connection.prepareStatement("INSERT INTO FILEINDEX VALUES ( ?)");
            }
            //System.out.println("Created table");
            System.out.println("Starting indexing");
            StringBuilder sb = new StringBuilder();
            sb.append("DRIVE || FILES        FOLDERS      TOTAL        TIME(ms)\n");
            sb.append("======||================================================\n");
            long driveStart;
            totalFileNumber = 0;
            totalFolderNumber = 0;
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
                preparedStatement.executeBatch();
            }
            sb.append("------||------------------------------------------------\n");
            sb.append(String.format("%-5s || %-11d  %-11d  %-11d  %-20d\n", "TOTAL",
                    totalFileNumber, totalFolderNumber, totalFileNumber + totalFolderNumber,
                    System.currentTimeMillis() - start));
            System.out.println("Done");
            System.out.println(sb.toString());
            //System.out.println("Time: " + ((double)(System.currentTimeMillis() - start)/1000) + "s");
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    private static void index(Path path) {
        if (debug && path.getNameCount() < 3) System.out.println(path);
        try {
            Files.list(path).forEach(filePath -> {
                long size = -1;
                if (DBFileIndexer.size) {
                    try {
                        size = Files.size(filePath);
                    } catch (IOException e) {
                        if (debug) System.err.println(e.getMessage());
                    }
                }
                try {
                    preparedStatement.setString(1, filePath.toString());
                    if (DBFileIndexer.size) preparedStatement.setLong(2, size);
                    preparedStatement.addBatch();
                } catch (SQLException e) {
                    if (debug) System.err.println(e);
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
            if (debug) System.err.println(e);
        }
    }
}
