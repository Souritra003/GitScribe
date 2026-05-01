package com.docpilot.docpilot_backend.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AnalyzerService {

    // ===== Data-driven maps — add new entries here, no logic changes needed =====

    private static final Map<String, String> EXTENSION_TO_LANGUAGE = new LinkedHashMap<>();
    private static final Map<String, String> FILENAME_TO_FRAMEWORK = new LinkedHashMap<>();
    private static final Map<String, String> PATH_KEYWORD_TO_STRUCTURE = new LinkedHashMap<>();
    private static final Map<String, String> NAME_KEYWORD_TO_FEATURE = new LinkedHashMap<>();

    static {
        // Languages — keyed by file extension
        EXTENSION_TO_LANGUAGE.put("java",   "Java");
        EXTENSION_TO_LANGUAGE.put("kt",     "Kotlin");
        EXTENSION_TO_LANGUAGE.put("scala",  "Scala");
        EXTENSION_TO_LANGUAGE.put("groovy", "Groovy");
        EXTENSION_TO_LANGUAGE.put("js",     "JavaScript");
        EXTENSION_TO_LANGUAGE.put("ts",     "TypeScript");
        EXTENSION_TO_LANGUAGE.put("jsx",    "JavaScript (React)");
        EXTENSION_TO_LANGUAGE.put("tsx",    "TypeScript (React)");
        EXTENSION_TO_LANGUAGE.put("mjs",    "JavaScript (ESM)");
        EXTENSION_TO_LANGUAGE.put("py",     "Python");
        EXTENSION_TO_LANGUAGE.put("rb",     "Ruby");
        EXTENSION_TO_LANGUAGE.put("php",    "PHP");
        EXTENSION_TO_LANGUAGE.put("go",     "Go");
        EXTENSION_TO_LANGUAGE.put("rs",     "Rust");
        EXTENSION_TO_LANGUAGE.put("c",      "C");
        EXTENSION_TO_LANGUAGE.put("h",      "C/C++ Header");
        EXTENSION_TO_LANGUAGE.put("cpp",    "C++");
        EXTENSION_TO_LANGUAGE.put("cc",     "C++");
        EXTENSION_TO_LANGUAGE.put("cxx",    "C++");
        EXTENSION_TO_LANGUAGE.put("cs",     "C#");
        EXTENSION_TO_LANGUAGE.put("fs",     "F#");
        EXTENSION_TO_LANGUAGE.put("swift",  "Swift");
        EXTENSION_TO_LANGUAGE.put("m",      "Objective-C");
        EXTENSION_TO_LANGUAGE.put("dart",   "Dart");
        EXTENSION_TO_LANGUAGE.put("lua",    "Lua");
        EXTENSION_TO_LANGUAGE.put("r",      "R");
        EXTENSION_TO_LANGUAGE.put("jl",     "Julia");
        EXTENSION_TO_LANGUAGE.put("ex",     "Elixir");
        EXTENSION_TO_LANGUAGE.put("exs",    "Elixir");
        EXTENSION_TO_LANGUAGE.put("erl",    "Erlang");
        EXTENSION_TO_LANGUAGE.put("hs",     "Haskell");
        EXTENSION_TO_LANGUAGE.put("clj",    "Clojure");
        EXTENSION_TO_LANGUAGE.put("cljs",   "ClojureScript");
        EXTENSION_TO_LANGUAGE.put("lisp",   "Lisp");
        EXTENSION_TO_LANGUAGE.put("sh",     "Shell");
        EXTENSION_TO_LANGUAGE.put("bash",   "Bash");
        EXTENSION_TO_LANGUAGE.put("zsh",    "Zsh");
        EXTENSION_TO_LANGUAGE.put("ps1",    "PowerShell");
        EXTENSION_TO_LANGUAGE.put("sql",    "SQL");
        EXTENSION_TO_LANGUAGE.put("graphql","GraphQL");
        EXTENSION_TO_LANGUAGE.put("proto",  "Protocol Buffers");
        EXTENSION_TO_LANGUAGE.put("tf",     "Terraform (HCL)");
        EXTENSION_TO_LANGUAGE.put("hcl",    "HCL");
        EXTENSION_TO_LANGUAGE.put("html",   "HTML");
        EXTENSION_TO_LANGUAGE.put("css",    "CSS");
        EXTENSION_TO_LANGUAGE.put("scss",   "SCSS");
        EXTENSION_TO_LANGUAGE.put("sass",   "Sass");
        EXTENSION_TO_LANGUAGE.put("less",   "Less");
        EXTENSION_TO_LANGUAGE.put("vue",    "Vue.js");
        EXTENSION_TO_LANGUAGE.put("svelte", "Svelte");
        EXTENSION_TO_LANGUAGE.put("xml",    "XML");
        EXTENSION_TO_LANGUAGE.put("yaml",   "YAML");
        EXTENSION_TO_LANGUAGE.put("yml",    "YAML");
        EXTENSION_TO_LANGUAGE.put("json",   "JSON");
        EXTENSION_TO_LANGUAGE.put("toml",   "TOML");
        EXTENSION_TO_LANGUAGE.put("ini",    "INI");
        EXTENSION_TO_LANGUAGE.put("md",     "Markdown");
        EXTENSION_TO_LANGUAGE.put("rst",    "reStructuredText");

        // Frameworks — keyed by exact filename (lowercase)
        FILENAME_TO_FRAMEWORK.put("pom.xml",            "Maven / Spring Boot");
        FILENAME_TO_FRAMEWORK.put("build.gradle",       "Gradle");
        FILENAME_TO_FRAMEWORK.put("build.gradle.kts",   "Gradle (Kotlin DSL)");
        FILENAME_TO_FRAMEWORK.put("package.json",       "Node.js");
        FILENAME_TO_FRAMEWORK.put("angular.json",       "Angular");
        FILENAME_TO_FRAMEWORK.put("next.config.js",     "Next.js");
        FILENAME_TO_FRAMEWORK.put("next.config.ts",     "Next.js");
        FILENAME_TO_FRAMEWORK.put("nuxt.config.js",     "Nuxt.js");
        FILENAME_TO_FRAMEWORK.put("vite.config.js",     "Vite");
        FILENAME_TO_FRAMEWORK.put("vite.config.ts",     "Vite");
        FILENAME_TO_FRAMEWORK.put("webpack.config.js",  "Webpack");
        FILENAME_TO_FRAMEWORK.put("requirements.txt",   "Python (pip)");
        FILENAME_TO_FRAMEWORK.put("pyproject.toml",     "Python (pyproject)");
        FILENAME_TO_FRAMEWORK.put("setup.py",           "Python (setuptools)");
        FILENAME_TO_FRAMEWORK.put("pipfile",            "Pipenv");
        FILENAME_TO_FRAMEWORK.put("cargo.toml",         "Rust (Cargo)");
        FILENAME_TO_FRAMEWORK.put("go.mod",             "Go Modules");
        FILENAME_TO_FRAMEWORK.put("gemfile",            "Ruby (Bundler)");
        FILENAME_TO_FRAMEWORK.put("composer.json",      "PHP (Composer)");
        FILENAME_TO_FRAMEWORK.put("pubspec.yaml",       "Flutter / Dart");
        FILENAME_TO_FRAMEWORK.put("mix.exs",            "Elixir (Mix)");
        FILENAME_TO_FRAMEWORK.put("dockerfile",         "Docker");
        FILENAME_TO_FRAMEWORK.put("docker-compose.yml", "Docker Compose");
        FILENAME_TO_FRAMEWORK.put("docker-compose.yaml","Docker Compose");
        FILENAME_TO_FRAMEWORK.put(".terraform.lock.hcl","Terraform");
        FILENAME_TO_FRAMEWORK.put("cmakelists.txt",     "CMake");
        FILENAME_TO_FRAMEWORK.put("makefile",           "Make");

        // Structure — keyed by path keyword (lowercase)
        PATH_KEYWORD_TO_STRUCTURE.put("controller",  "controllers");
        PATH_KEYWORD_TO_STRUCTURE.put("service",     "services");
        PATH_KEYWORD_TO_STRUCTURE.put("repository",  "repositories");
        PATH_KEYWORD_TO_STRUCTURE.put("dao",         "repositories");
        PATH_KEYWORD_TO_STRUCTURE.put("model",       "models");
        PATH_KEYWORD_TO_STRUCTURE.put("entity",      "models");
        PATH_KEYWORD_TO_STRUCTURE.put("dto",         "dtos");
        PATH_KEYWORD_TO_STRUCTURE.put("middleware",  "middlewares");
        PATH_KEYWORD_TO_STRUCTURE.put("interceptor", "interceptors");
        PATH_KEYWORD_TO_STRUCTURE.put("util",        "utilities");
        PATH_KEYWORD_TO_STRUCTURE.put("helper",      "utilities");
        PATH_KEYWORD_TO_STRUCTURE.put("config",      "configurations");
        PATH_KEYWORD_TO_STRUCTURE.put("test",        "tests");
        PATH_KEYWORD_TO_STRUCTURE.put("spec",        "tests");
        PATH_KEYWORD_TO_STRUCTURE.put("migration",   "migrations");
        PATH_KEYWORD_TO_STRUCTURE.put("schema",      "schemas");

        // Features — keyed by filename keyword (lowercase)
        NAME_KEYWORD_TO_FEATURE.put("auth",        "Authentication");
        NAME_KEYWORD_TO_FEATURE.put("oauth",       "OAuth / SSO");
        NAME_KEYWORD_TO_FEATURE.put("jwt",         "JWT / Token Auth");
        NAME_KEYWORD_TO_FEATURE.put("user",        "User Management");
        NAME_KEYWORD_TO_FEATURE.put("role",        "Role & Permission Management");
        NAME_KEYWORD_TO_FEATURE.put("permission",  "Role & Permission Management");
        NAME_KEYWORD_TO_FEATURE.put("payment",     "Payment System");
        NAME_KEYWORD_TO_FEATURE.put("invoice",     "Invoicing");
        NAME_KEYWORD_TO_FEATURE.put("order",       "Order Management");
        NAME_KEYWORD_TO_FEATURE.put("cart",        "Shopping Cart");
        NAME_KEYWORD_TO_FEATURE.put("product",     "Product Catalog");
        NAME_KEYWORD_TO_FEATURE.put("inventory",   "Inventory Management");
        NAME_KEYWORD_TO_FEATURE.put("notification","Notification System");
        NAME_KEYWORD_TO_FEATURE.put("email",       "Email Integration");
        NAME_KEYWORD_TO_FEATURE.put("sms",         "SMS Integration");
        NAME_KEYWORD_TO_FEATURE.put("api",         "API Layer");
        NAME_KEYWORD_TO_FEATURE.put("webhook",     "Webhook Support");
        NAME_KEYWORD_TO_FEATURE.put("upload",      "File Upload");
        NAME_KEYWORD_TO_FEATURE.put("storage",     "File Storage");
        NAME_KEYWORD_TO_FEATURE.put("cache",       "Caching");
        NAME_KEYWORD_TO_FEATURE.put("queue",       "Message Queue");
        NAME_KEYWORD_TO_FEATURE.put("scheduler",   "Job Scheduling");
        NAME_KEYWORD_TO_FEATURE.put("report",      "Reporting");
        NAME_KEYWORD_TO_FEATURE.put("analytics",   "Analytics");
        NAME_KEYWORD_TO_FEATURE.put("search",      "Search");
        NAME_KEYWORD_TO_FEATURE.put("config",      "Configuration Management");
        NAME_KEYWORD_TO_FEATURE.put("log",         "Logging");
        NAME_KEYWORD_TO_FEATURE.put("audit",       "Audit Trail");
        NAME_KEYWORD_TO_FEATURE.put("health",      "Health Check");
        NAME_KEYWORD_TO_FEATURE.put("monitor",     "Monitoring");
        NAME_KEYWORD_TO_FEATURE.put("migration",   "Database Migration");
        NAME_KEYWORD_TO_FEATURE.put("seed",        "Database Seeding");
        NAME_KEYWORD_TO_FEATURE.put("chat",        "Chat / Messaging");
        NAME_KEYWORD_TO_FEATURE.put("socket",      "WebSocket / Real-time");
        NAME_KEYWORD_TO_FEATURE.put("geo",         "Geolocation");
        NAME_KEYWORD_TO_FEATURE.put("map",         "Mapping");
        NAME_KEYWORD_TO_FEATURE.put("i18n",        "Internationalization");
        NAME_KEYWORD_TO_FEATURE.put("locale",      "Internationalization");
        NAME_KEYWORD_TO_FEATURE.put("test",        "Testing");
    }

    // ===== Core analyze method — no hardcoding, purely driven by the maps above =====

    public Map<String, Object> analyze(List<Map<String, Object>> files) {

        Map<String, Integer> fileTypes  = new HashMap<>();
        Set<String>          languages  = new LinkedHashSet<>();
        Set<String>          frameworks = new LinkedHashSet<>();
        Map<String, Integer> structure  = new LinkedHashMap<>();
        Set<String>          features   = new LinkedHashSet<>();

        for (Map<String, Object> file : files) {

            String name = (String) file.get("name");
            String path = (String) file.get("path");

            if (name == null || path == null) continue;

            String lowerName = name.toLowerCase();
            String lowerPath = path.toLowerCase();
            String extension = getExtension(lowerName);

            // File type count
            fileTypes.merge(extension, 1, Integer::sum);

            // Language detection via extension map
            String language = EXTENSION_TO_LANGUAGE.get(extension);
            if (language != null) languages.add(language);

            // Framework detection via exact filename match
            String framework = FILENAME_TO_FRAMEWORK.get(lowerName);
            if (framework != null) frameworks.add(framework);

            // Structure detection via path keyword scan
            for (Map.Entry<String, String> entry : PATH_KEYWORD_TO_STRUCTURE.entrySet()) {
                if (lowerPath.contains(entry.getKey())) {
                    structure.merge(entry.getValue(), 1, Integer::sum);
                }
            }

            // Feature detection via filename keyword scan
            for (Map.Entry<String, String> entry : NAME_KEYWORD_TO_FEATURE.entrySet()) {
                if (lowerName.contains(entry.getKey())) {
                    features.add(entry.getValue());
                }
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalFiles", files.size());
        result.put("fileTypes",  fileTypes);
        result.put("languages",  languages);
        result.put("frameworks", frameworks);
        result.put("structure",  structure);
        result.put("features",   features);
        return result;
    }

    private String getExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        return index == -1 ? "unknown" : fileName.substring(index + 1);
    }
}