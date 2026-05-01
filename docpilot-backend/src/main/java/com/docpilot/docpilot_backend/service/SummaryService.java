package com.docpilot.docpilot_backend.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SummaryService {

    public String buildSummary(String repoName,
                               Map<String, Object> analysis,
                               Map<String, Object> tree) {

        return """
                You are a senior software architect.

                Analyze the following project and generate professional documentation.

                Project Name: %s

                Languages: %s
                Frameworks: %s
                Features: %s
                Structure: %s

                Directory Structure:
                %s

                Generate:
                1. Project Overview
                2. Key Features
                3. Tech Stack
                4. Architecture
                5. Folder Explanation
                It must look very professional
                """.formatted(
                repoName,
                analysis.get("languages"),
                analysis.get("frameworks"),
                analysis.get("features"),
                analysis.get("structure"),
                prettyTree(tree, 0)

        );
    }

    private String prettyTree(Map<String, Object> tree, int level) {
        StringBuilder sb = new StringBuilder();
        String indent = "  ".repeat(level);

        for (Map.Entry<String, Object> entry : tree.entrySet()) {

            if (entry.getKey().equals("files")) {
                for (String file : (List<String>) entry.getValue()) {
                    sb.append(indent).append("- ").append(file).append("\n");
                }
            } else {
                sb.append(indent).append("📁 ").append(entry.getKey()).append("\n");
                sb.append(prettyTree((Map<String, Object>) entry.getValue(), level + 1));
            }
        }
        return sb.toString();
    }
}