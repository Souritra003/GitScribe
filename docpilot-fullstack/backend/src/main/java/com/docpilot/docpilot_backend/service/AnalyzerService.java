package com.docpilot.docpilot_backend.service;

import com.docpilot.docpilot_backend.model.RepositoryAnalysis;
import com.docpilot.docpilot_backend.model.RepositorySnapshot;
import com.docpilot.docpilot_backend.model.SourceFile;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AnalyzerService {

    private static final Pattern CLASS_PATTERN =
            Pattern.compile("\\b(class|interface|enum|record|struct|trait|type)\\s+([A-Za-z_$][\\w$]*)");

    private static final Pattern FUNCTION_PATTERN =
            Pattern.compile("\\b([A-Za-z_$][\\w$]*)\\s*\\([^\\n;{}]{0,120}\\)\\s*(\\{|=>)");

    private static final Pattern REST_PATTERN =
            Pattern.compile("(?i)(GET|POST|PUT|PATCH|DELETE)\\s*[\\(\"']?(/[^\\\"'\\)\\s]+)");

    public RepositoryAnalysis analyze(RepositorySnapshot snapshot) {
        Map<String, Integer> languages = new TreeMap<>();
        Set<String> frameworks = new TreeSet<>();
        Set<String> buildTools = new TreeSet<>();
        Set<String> features = new TreeSet<>();
        Set<String> entryPoints = new TreeSet<>();
        Set<String> endpoints = new TreeSet<>();
        Set<String> types = new TreeSet<>();
        Set<String> functions = new TreeSet<>();
        List<String> importantFiles = new ArrayList<>();

        long totalBytes = 0;

        for (String path : snapshot.getAllPaths()) {
            if (isImportantFile(path)) {
                importantFiles.add(path);
            }
        }

        for (SourceFile file : snapshot.getSourceFiles()) {
            languages.merge(file.getLanguage(), 1, Integer::sum);
            totalBytes += file.getSize();

            String path = file.getPath().toLowerCase(Locale.ROOT);
            String content = file.getContent();

            detectToolsAndFrameworks(path, content, frameworks, buildTools, features);
            detectEntryPoints(path, content, entryPoints);
            detectEndpoints(content, endpoints);
            detectTypes(content, types);
            detectFunctions(content, functions);
        }

        if (languages.containsKey("Java")) features.add("Java-based backend or application code");
        if (languages.containsKey("Python")) features.add("Python-based application or scripting code");
        if (languages.containsKey("JavaScript") || languages.containsKey("TypeScript")) {
            features.add("JavaScript/TypeScript application code");
        }
        if (languages.containsKey("HTML") || languages.containsKey("CSS")) {
            features.add("Web user-interface assets");
        }
        if (languages.containsKey("SQL")) features.add("Database/query definitions");

        return new RepositoryAnalysis(
                snapshot.getAllPaths().size(),
                totalBytes,
                languages,
                new ArrayList<>(frameworks),
                new ArrayList<>(buildTools),
                new ArrayList<>(features),
                new ArrayList<>(entryPoints),
                new ArrayList<>(endpoints),
                limit(new ArrayList<>(types), 80),
                limit(new ArrayList<>(functions), 120),
                importantFiles.stream().sorted().limit(80).toList()
        );
    }

    private void detectToolsAndFrameworks(
            String path,
            String content,
            Set<String> frameworks,
            Set<String> buildTools,
            Set<String> features) {

        String lower = content.toLowerCase(Locale.ROOT);

        if (path.endsWith("pom.xml")) {
            buildTools.add("Maven");
            if (lower.contains("spring-boot")) frameworks.add("Spring Boot");
            if (lower.contains("spring-security")) frameworks.add("Spring Security");
            if (lower.contains("spring-data-jpa")) frameworks.add("Spring Data JPA");
            if (lower.contains("spring-ai")) frameworks.add("Spring AI");
        }

        if (path.endsWith("build.gradle") || path.endsWith("build.gradle.kts")) {
            buildTools.add("Gradle");
            if (lower.contains("spring-boot")) frameworks.add("Spring Boot");
        }

        if (path.endsWith("package.json")) {
            buildTools.add("npm / Node.js");
            if (lower.contains("\"react\"")) frameworks.add("React");
            if (lower.contains("\"next\"")) frameworks.add("Next.js");
            if (lower.contains("\"express\"")) frameworks.add("Express");
            if (lower.contains("\"vue\"")) frameworks.add("Vue");
            if (lower.contains("\"angular\"")) frameworks.add("Angular");
        }

        if (path.endsWith("requirements.txt") || path.endsWith("pyproject.toml")) {
            buildTools.add("Python package management");
            if (lower.contains("fastapi")) frameworks.add("FastAPI");
            if (lower.contains("django")) frameworks.add("Django");
            if (lower.contains("flask")) frameworks.add("Flask");
            if (lower.contains("langchain")) frameworks.add("LangChain");
        }

        if (path.endsWith("go.mod")) {
            buildTools.add("Go modules");
        }

        if (path.endsWith("cargo.toml")) {
            buildTools.add("Cargo");
            frameworks.add("Rust ecosystem");
        }

        if (path.endsWith("dockerfile") || path.contains("docker-compose")) {
            frameworks.add("Docker");
            features.add("Containerization");
        }

        if (lower.contains("jwt") || lower.contains("jsonwebtoken")) {
            features.add("JWT-based authentication");
        }

        if (lower.contains("oauth")) {
            features.add("OAuth integration");
        }

        if (lower.contains("restcontroller")
                || lower.contains("@getmapping")
                || lower.contains("@postmapping")
                || lower.contains("fastapi")) {
            features.add("REST API");
        }

        if (lower.contains("websocket")) features.add("WebSocket communication");
        if (lower.contains("redis")) features.add("Redis integration");
        if (lower.contains("kafka")) features.add("Kafka/event streaming");
        if (lower.contains("postgres")) features.add("PostgreSQL integration");
        if (lower.contains("mysql")) features.add("MySQL integration");
        if (lower.contains("mongodb")) features.add("MongoDB integration");
        if (lower.contains("gemini") || lower.contains("openai")) {
            features.add("Generative AI integration");
        }
        if (lower.contains("langchain") || lower.contains("retrievalaugmented")
                || lower.contains("retrieval-augmented")) {
            features.add("RAG/LLM application patterns");
        }
    }

    private void detectEntryPoints(String path, String content, Set<String> entryPoints) {
        String lower = content.toLowerCase(Locale.ROOT);

        if (path.endsWith("application.java")
                || path.endsWith("application.kt")
                || lower.contains("springapplication.run(")) {
            entryPoints.add(path);
        }

        if (path.endsWith("main.py") || path.endsWith("__main__.py")) {
            entryPoints.add(path);
        }

        if (path.endsWith("main.ts") || path.endsWith("main.tsx")
                || path.endsWith("index.js") || path.endsWith("index.tsx")) {
            entryPoints.add(path);
        }

        if (path.endsWith("dockerfile")) {
            entryPoints.add(path);
        }
    }

    private void detectEndpoints(String content, Set<String> endpoints) {
        Matcher restMatcher = REST_PATTERN.matcher(content);
        while (restMatcher.find() && endpoints.size() < 100) {
            endpoints.add(restMatcher.group(1).toUpperCase(Locale.ROOT)
                    + " " + restMatcher.group(2));
        }

        Pattern spring = Pattern.compile(
                "@(GetMapping|PostMapping|PutMapping|PatchMapping|DeleteMapping)\\s*\\((?:value\\s*=\\s*)?[\"']([^\"']+)"
        );
        Matcher springMatcher = spring.matcher(content);

        while (springMatcher.find() && endpoints.size() < 100) {
            endpoints.add(springMatcher.group(1).replace("Mapping", "").toUpperCase(Locale.ROOT)
                    + " " + springMatcher.group(2));
        }
    }

    private void detectTypes(String content, Set<String> types) {
        Matcher matcher = CLASS_PATTERN.matcher(content);
        while (matcher.find() && types.size() < 150) {
            types.add(matcher.group(1) + " " + matcher.group(2));
        }
    }

    private void detectFunctions(String content, Set<String> functions) {
        Matcher matcher = FUNCTION_PATTERN.matcher(content);
        while (matcher.find() && functions.size() < 200) {
            String name = matcher.group(1);
            if (!Set.of("if", "for", "while", "switch", "catch", "function").contains(name)) {
                functions.add(name + "()");
            }
        }
    }

    private boolean isImportantFile(String path) {
        String lower = path.toLowerCase(Locale.ROOT);
        return lower.equals("readme.md")
                || lower.endsWith("pom.xml")
                || lower.endsWith("package.json")
                || lower.endsWith("requirements.txt")
                || lower.endsWith("pyproject.toml")
                || lower.endsWith("build.gradle")
                || lower.endsWith("build.gradle.kts")
                || lower.endsWith("dockerfile")
                || lower.endsWith("docker-compose.yml")
                || lower.endsWith("docker-compose.yaml")
                || lower.endsWith("go.mod")
                || lower.endsWith("cargo.toml");
    }

    private <T> List<T> limit(List<T> values, int max) {
        return values.stream().limit(max).toList();
    }
}
