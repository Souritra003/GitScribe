package com.docpilot.docpilot_backend.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DocGeneratorService {

    public String generateDoc(String repoName,
                              Map<String, Object> analysis,
                              Map<String, Object> tree,
                              String aiOutput) {

        // 🔥 If AI works → return AI output directly
        if (aiOutput != null && !aiOutput.isBlank()) {
            return aiOutput;
        }

        // ⚠️ Fallback → your existing logic
        StringBuilder doc = new StringBuilder();

        doc.append("📘 Project Documentation\n");
        doc.append("=".repeat(50)).append("\n\n");

        doc.append("Project Name : ").append(repoName).append("\n\n");

        appendSection(doc, "Languages Detected", analysis.get("languages"));
        appendSection(doc, "Frameworks & Tools", analysis.get("frameworks"));
        appendSection(doc, "Detected Features", analysis.get("features"));

        // 🌳 Add Tree Structure
        doc.append("Directory Structure:\n");
        doc.append(formatTree(tree, 0)).append("\n");

        doc.append("Summary:\n");
        doc.append(buildSummary(repoName, analysis));

        return doc.toString();
    }

    // ===== TREE FORMATTER =====
    private String formatTree(Map<String, Object> tree, int level) {
        StringBuilder sb = new StringBuilder();

        String indent = "  ".repeat(level);

        for (Map.Entry<String, Object> entry : tree.entrySet()) {

            if (entry.getKey().equals("files")) {
                List<String> files = (List<String>) entry.getValue();
                for (String file : files) {
                    sb.append(indent).append("- ").append(file).append("\n");
                }
            } else {
                sb.append(indent).append("📁 ").append(entry.getKey()).append("\n");
                sb.append(formatTree((Map<String, Object>) entry.getValue(), level + 1));
            }
        }

        return sb.toString();
    }

    // ===== EXISTING METHODS =====

    private void appendSection(StringBuilder doc, String title, Object value) {
        if (value instanceof Collection<?> items && !items.isEmpty()) {
            doc.append(title).append(":\n");
            items.forEach(item -> doc.append("  - ").append(item).append("\n"));
            doc.append("\n");
        }
    }

    private String buildSummary(String repoName, Map<String, Object> analysis) {
        StringBuilder summary = new StringBuilder();
        summary.append("  ").append(repoName).append(" is a");

        Object langs = analysis.get("languages");
        if (langs instanceof Collection<?> langList && !langList.isEmpty()) {
            summary.append(" ").append(String.join(", ", toStringList(langList)));
            summary.append(" based");
        }

        Object fw = analysis.get("frameworks");
        if (fw instanceof Collection<?> fwList && !fwList.isEmpty()) {
            summary.append(" project using ").append(String.join(", ", toStringList(fwList)));
        } else {
            summary.append(" project");
        }

        summary.append(".\n");
        return summary.toString();
    }

    private List<String> toStringList(Collection<?> collection) {
        return collection.stream().map(Object::toString).toList();
    }
}