package com.docpilot.docpilot_backend.service;

import com.docpilot.docpilot_backend.model.RepositoryAnalysis;
import com.docpilot.docpilot_backend.model.RepositorySnapshot;
import com.docpilot.docpilot_backend.model.SourceFile;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class SummaryService {

    public String buildPrompt(
            RepositorySnapshot snapshot,
            RepositoryAnalysis analysis,
            String tree) {

        String sourceContext = snapshot.getSourceFiles().stream()
                .map(file -> """
                        FILE: %s
                        LANGUAGE: %s
                        CONTENT:
                        %s
                        """.formatted(
                        file.getPath(),
                        file.getLanguage(),
                        trimContent(file.getContent(), 12000)
                ))
                .collect(Collectors.joining("\n\n"));

        return """
                You are a senior software architect and technical writer.

                Analyze the supplied repository evidence and generate professional
                Markdown documentation.

                IMPORTANT RULES:
                - Only claim facts supported by the supplied repository evidence.
                - Do not invent technologies, APIs, architecture, database tables,
                  deployment methods, or features.
                - If something cannot be determined, say "Not identified from the analyzed files."
                - Explain why components exist and how they interact.
                - Prefer concrete file/class/function names when available.
                - Treat the repository tree and source snippets as evidence.
                - Never expose API keys, tokens, passwords, or secret values.
                - Do not reproduce large source files.

                REPOSITORY
                Name: %s
                Full name: %s
                Description: %s
                Default branch: %s

                ANALYSIS
                Total repository paths: %d
                Source files analyzed: %d
                Bytes analyzed: %d

                Languages:
                %s

                Frameworks:
                %s

                Build tools:
                %s

                Detected features:
                %s

                Entry points:
                %s

                API endpoints:
                %s

                Classes/types:
                %s

                Functions/methods:
                %s

                Important files:
                %s

                DIRECTORY TREE
                %s

                SOURCE EVIDENCE
                %s

                Generate these sections:
                # Project Name
                ## Overview
                ## Key Features
                ## Technology Stack
                ## Architecture
                ## Application Flow
                ## API / Interfaces
                ## Important Components
                ## Project Structure
                ## Configuration
                ## Setup and Run
                ## Testing
                ## Deployment Notes
                ## Limitations / Unknowns

                Keep the result clear and useful for a developer who has never
                seen this repository.
                """.formatted(
                snapshot.getName(),
                snapshot.getFullName(),
                snapshot.getDescription(),
                snapshot.getDefaultBranch(),
                snapshot.getAllPaths().size(),
                snapshot.getSourceFiles().size(),
                analysis.getTotalBytesAnalyzed(),
                analysis.getLanguages(),
                analysis.getFrameworks(),
                analysis.getBuildTools(),
                analysis.getFeatures(),
                analysis.getEntryPoints(),
                analysis.getApiEndpoints(),
                analysis.getClassesOrTypes(),
                analysis.getFunctionsOrMethods(),
                analysis.getImportantFiles(),
                tree,
                sourceContext
        );
    }

    private String trimContent(String content, int maxChars) {
        if (content == null) return "";
        if (content.length() <= maxChars) return content;
        return content.substring(0, maxChars)
                + "\n...[content truncated by DocPilot]...";
    }
}
