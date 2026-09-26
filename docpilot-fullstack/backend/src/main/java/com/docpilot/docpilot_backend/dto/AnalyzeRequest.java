package com.docpilot.docpilot_backend.dto;

import jakarta.validation.constraints.NotBlank;

public class AnalyzeRequest {
    @NotBlank
    private String owner;

    @NotBlank
    private String repo;

    public AnalyzeRequest() {}

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public String getRepo() { return repo; }
    public void setRepo(String repo) { this.repo = repo; }
}
