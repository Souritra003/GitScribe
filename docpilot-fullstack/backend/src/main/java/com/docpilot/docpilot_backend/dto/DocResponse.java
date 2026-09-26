package com.docpilot.docpilot_backend.dto;

public class DocResponse {
    private String repository;
    private String documentation;

    public DocResponse() {}

    public DocResponse(String repository, String documentation) {
        this.repository = repository;
        this.documentation = documentation;
    }

    public String getRepository() { return repository; }
    public String getDocumentation() { return documentation; }
}
