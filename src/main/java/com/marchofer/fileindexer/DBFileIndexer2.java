package com.marchofer.fileindexer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributes;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Arrays;

public class DBFileIndexer2 {
    private static boolean debug = false;
    private static boolean hash = false;
    private static boolean size = true;

    private static Connection connection;
    private static Statement statement;
    private static PreparedStatement preparedStatement;

    private static int folderNumber;
    private static int fileNumber;
    private static int totalFolderNumber;
    private static int totalFileNumber;
    private static MessageDigest md;

    private static long appStart = System.currentTimeMillis();
    public static void main(String[] args) {
        if (Arrays.asList(args).contains("-h")) {
            System.out.println("FileIndexer CSV options");
            System.out.println("-d: prints debug messages");
            System.out.println("-m: include md5 hash in output");
            System.out.println("-s: exclude size from output");
            System.exit(0);
        }
        if (Arrays.asList(args).contains("-d")) debug = true;
        if (Arrays.asList(args).contains("-m")) hash = true;
        if (Arrays.asList(args).contains("-s")) size = false;
        if (hash) {
            try {
                md = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
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
            if (size && hash) sql = "DROP TABLE IF EXISTS FileIndex; " +
                    "CREATE TABLE FileIndex (" +
                    " path TEXT, " +
                    " size BIGINT," +
                    " hash RAW(16) );";
            if (!size && hash) sql = "DROP TABLE IF EXISTS FileIndex; " +
                    "CREATE TABLE FileIndex (" +
                    " path TEXT, " +
                    " hash RAW(16) );";
            if (!size) sql = "DROP TABLE IF EXISTS FileIndex; " +
                    "CREATE TABLE FileIndex (" +
                    " path TEXT );";
            statement.executeUpdate(sql);
            if (size && hash) {
                preparedStatement = connection.prepareStatement("INSERT INTO FILEINDEX VALUES ( ?, ?, ?)");
            } else if (size || hash) {
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
                BasicFileAttributes attr = null;
                try {
                    attr = Files.readAttributes(filePath, BasicFileAttributes.class);
                } catch (IOException e) {
                    if (debug) System.err.println(e);
                }
                if (hash) {
                    InputStream is = null;
                    try {
                        is = Files.newInputStream(filePath);
                    } catch (IOException e) {
                        if (debug) System.err.println(e);
                    }
                    new DigestInputStream(is, md);
                }
                if (attr == null) return;
                try {
                    preparedStatement.setString(1, filePath.toString());
                    if (DBFileIndexer2.size) preparedStatement.setLong(2, attr.size());
                    if (hash && DBFileIndexer2.size) preparedStatement.setBytes(3, md.digest());
                    if (hash && !DBFileIndexer2.size) preparedStatement.setBytes(2, md.digest());
                    preparedStatement.addBatch();
                } catch (SQLException e) {
                    if (debug) System.err.println(e);
                }
                if (attr.isDirectory()) {
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
