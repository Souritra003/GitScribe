package com.docpilot.docpilot_backend.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TreeStructureService {

    public String buildTree(List<String> paths) {
        TreeNode root = new TreeNode("");

        paths.stream()
                .filter(Objects::nonNull)
                .sorted()
                .forEach(path -> add(root, path));

        StringBuilder result = new StringBuilder();
        for (TreeNode child : root.children.values()) {
            render(child, "", result);
        }
        return result.toString();
    }

    private void add(TreeNode root, String path) {
        TreeNode current = root;

        for (String part : path.replace('\\', '/').split("/")) {
            if (part.isBlank()) continue;
            current = current.children.computeIfAbsent(
                    part,
                    ignored -> new TreeNode(part)
            );
        }
    }

    private void render(TreeNode node, String indent, StringBuilder out) {
        boolean file = node.children.isEmpty();

        out.append(indent)
                .append(file ? "├── " : "├── ")
                .append(node.name)
                .append('\n');

        if (!file) {
            for (TreeNode child : node.children.values()) {
                render(child, indent + "│   ", out);
            }
        }
    }

    private static class TreeNode {
        private final String name;
        private final Map<String, TreeNode> children = new TreeMap<>();

        private TreeNode(String name) {
            this.name = name;
        }
    }
}
