package com.marchofer.fileindexer;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.stream.Stream;

public class FileViewer extends JFrame {
    private JTree tree;
    private JScrollPane scrollPane;
    private static String filename;

    private FileViewer() {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Files");
        /*rootNode.add(new DefaultMutableTreeNode("Test"));
        rootNode.add(new DefaultMutableTreeNode("Test2"));
        DefaultMutableTreeNode test3 = new DefaultMutableTreeNode("Test3");
        test3.add(new DefaultMutableTreeNode("Test4"));
        rootNode.add(test3);*/
        tree = new JTree(rootNode);
        scrollPane = new JScrollPane(tree);
        try  {
            for (String line: Files.readAllLines(Paths.get(filename))) {
                String[] values = line.split(";");
                String[] path = values[0].split("\\\\");
                System.out.println(line);
                for (int i = 1; i < path.length; i++) {
                    TreePath treePath = find(rootNode, Arrays.copyOfRange(path, 0, i), 0);
                    if (treePath == null) {
                        if (i > 1) {
                            TreePath oldTreeNode = find(rootNode, Arrays.copyOfRange(path, 0, i - 1), 0);
                            DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                                    (oldTreeNode != null ? oldTreeNode.getLastPathComponent() : null);
                            if (node != null) {
                                node.add(createNodes(Arrays.copyOfRange(path, i - 1, path.length), 0));
                            }
                        } else {
                            TreePath oldTreeNode = find(rootNode, new String[]{path[0]}, 0);
                            DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                                    (oldTreeNode != null ? oldTreeNode.getLastPathComponent() : null);
                            if (node != null) {
                                node.add(createNodes(Arrays.copyOfRange(path, i - 1, path.length), 0));
                            }
                        }
                    }
                }
                if (path.length == 1) rootNode.add(createNodes(Arrays.copyOfRange(path, 0, 1), 0));
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        this.add(scrollPane);
        this.pack();
        this.setVisible(true);
        /*TreePath path = find(rootNode, new String[]{"Test3", "Test4"}, 0);
        if (path != null) {
            System.out.println(path);
            tree.setSelectionPath(path);
            tree.scrollPathToVisible(path);
        }*/
    }

    private TreePath find(DefaultMutableTreeNode rootNode, String[] path, int i) {
        @SuppressWarnings("unchecked")
        Enumeration<DefaultMutableTreeNode> childNodes = rootNode.children();
        while (childNodes.hasMoreElements()) {
            DefaultMutableTreeNode childNode = childNodes.nextElement();
            if (childNode.toString().equalsIgnoreCase(path[i]) && (i + 1) == path.length) {
                return new TreePath(childNode.getPath());
            } else if (childNode.toString().equalsIgnoreCase(path[i])) {
                return find(childNode, path, i + 1);
            }
        }
        return null;
    }

    private DefaultMutableTreeNode createNodes(String[] path, int i) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(path[i]);
        if (i == (path.length - 1)) {
            return node;
        } else {
            node.add(createNodes(path, i + 1));
            return node;
        }
    }

    public static void main(String[] args) {
        /*if (args.length < 1) {
            System.err.println("Missing csv file path argument");
            System.exit(1);
        }*/
        filename = "C:\\Users\\Marc Hofer\\Downloads\\index.csv";
        SwingUtilities.invokeLater(FileViewer::new);
    }
}
