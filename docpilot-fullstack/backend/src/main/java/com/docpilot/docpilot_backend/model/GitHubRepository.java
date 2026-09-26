package com.docpilot.docpilot_backend.model;

public class GitHubRepository {
    private String name;
    private String fullName;
    private String htmlUrl;
    private String description;
    private String defaultBranch;
    private boolean privateRepository;

    public GitHubRepository() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getHtmlUrl() { return htmlUrl; }
    public void setHtmlUrl(String htmlUrl) { this.htmlUrl = htmlUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDefaultBranch() { return defaultBranch; }
    public void setDefaultBranch(String defaultBranch) { this.defaultBranch = defaultBranch; }

    public boolean isPrivateRepository() { return privateRepository; }
    public void setPrivateRepository(boolean privateRepository) { this.privateRepository = privateRepository; }
}
