package com.docpilot.docpilot_backend.model;

import java.util.List;

public class RepositorySnapshot {
    private String owner;
    private String name;
    private String fullName;
    private String description;
    private String defaultBranch;
    private String htmlUrl;
    private List<String> allPaths;
    private List<SourceFile> sourceFiles;

    public RepositorySnapshot() {}

    public RepositorySnapshot(String owner, String name, String fullName,
                              String description, String defaultBranch,
                              String htmlUrl, List<String> allPaths,
                              List<SourceFile> sourceFiles) {
        this.owner = owner;
        this.name = name;
        this.fullName = fullName;
        this.description = description;
        this.defaultBranch = defaultBranch;
        this.htmlUrl = htmlUrl;
        this.allPaths = allPaths;
        this.sourceFiles = sourceFiles;
    }

    public String getOwner() { return owner; }
    public String getName() { return name; }
    public String getFullName() { return fullName; }
    public String getDescription() { return description; }
    public String getDefaultBranch() { return defaultBranch; }
    public String getHtmlUrl() { return htmlUrl; }
    public List<String> getAllPaths() { return allPaths; }
    public List<SourceFile> getSourceFiles() { return sourceFiles; }
}
