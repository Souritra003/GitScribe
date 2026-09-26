package com.docpilot.docpilot_backend.model;

public class SourceFile {
    private String path;
    private String language;
    private long size;
    private String content;

    public SourceFile() {}

    public SourceFile(String path, String language, long size, String content) {
        this.path = path;
        this.language = language;
        this.size = size;
        this.content = content;
    }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
