package com.docpilot.docpilot_backend.model;

import java.util.List;
import java.util.Map;

public class RepositoryAnalysis {
    private int totalFiles;
    private long totalBytesAnalyzed;
    private Map<String, Integer> languages;
    private List<String> frameworks;
    private List<String> buildTools;
    private List<String> features;
    private List<String> entryPoints;
    private List<String> apiEndpoints;
    private List<String> classesOrTypes;
    private List<String> functionsOrMethods;
    private List<String> importantFiles;

    public RepositoryAnalysis() {}

    public RepositoryAnalysis(int totalFiles, long totalBytesAnalyzed,
                              Map<String, Integer> languages,
                              List<String> frameworks,
                              List<String> buildTools,
                              List<String> features,
                              List<String> entryPoints,
                              List<String> apiEndpoints,
                              List<String> classesOrTypes,
                              List<String> functionsOrMethods,
                              List<String> importantFiles) {
        this.totalFiles = totalFiles;
        this.totalBytesAnalyzed = totalBytesAnalyzed;
        this.languages = languages;
        this.frameworks = frameworks;
        this.buildTools = buildTools;
        this.features = features;
        this.entryPoints = entryPoints;
        this.apiEndpoints = apiEndpoints;
        this.classesOrTypes = classesOrTypes;
        this.functionsOrMethods = functionsOrMethods;
        this.importantFiles = importantFiles;
    }

    public int getTotalFiles() { return totalFiles; }
    public long getTotalBytesAnalyzed() { return totalBytesAnalyzed; }
    public Map<String, Integer> getLanguages() { return languages; }
    public List<String> getFrameworks() { return frameworks; }
    public List<String> getBuildTools() { return buildTools; }
    public List<String> getFeatures() { return features; }
    public List<String> getEntryPoints() { return entryPoints; }
    public List<String> getApiEndpoints() { return apiEndpoints; }
    public List<String> getClassesOrTypes() { return classesOrTypes; }
    public List<String> getFunctionsOrMethods() { return functionsOrMethods; }
    public List<String> getImportantFiles() { return importantFiles; }
}
