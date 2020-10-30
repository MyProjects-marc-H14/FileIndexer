package com.marchofer.fileindexer;

import java.sql.*;

public class DuplicateFinder {
    private static Connection connection;
    private static Statement statement;

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static void main(String[] args) {
        /*if (args.length < 1) {
            System.err.println("Error: Missing db file path argument");
            System.exit(1);
        }*/
        try {
            Class.forName("org.h2.Driver");
            System.out.println("Connecting to database");
            connection = DriverManager.getConnection("jdbc:h2:./index");
            statement = connection.createStatement();
        } catch (ClassNotFoundException e) {
            System.err.println(e.getMessage());
            System.exit(2);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            System.err.println("Connection to db failed, check if path is valid");
            System.exit(3);
        }
        String sql = "SELECT  path, size, hash, COUNT(*) FROM FILEINDEX\n" +
                "GROUP BY size, hash\n" +
                "HAVING COUNT(*) > 1\n";
        try {
            ResultSet rs = statement.executeQuery(sql);
            System.out.println("Duplicates:");
            System.out.println("HASH                             SIZE                 PATH\n");
            while(rs.next()){
                String path = rs.getString("path");
                long size = rs.getLong("size");
                byte[] hash = rs.getBytes("hash");
                System.out.println(String.format("%-32s %-20d %s", bytesToHex(hash), size, path));
            }
            rs.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
