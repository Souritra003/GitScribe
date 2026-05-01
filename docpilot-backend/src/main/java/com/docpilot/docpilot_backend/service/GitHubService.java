package com.docpilot.docpilot_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class GitHubService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${github.token}")
    private String githubToken;

    public List<Map<String, Object>> fetchAllFiles(String owner, String repo, String path) {

        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/contents/" + path;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + githubToken);
        headers.set("Accept", "application/vnd.github+json");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                List.class
        );

        List<Map<String, Object>> contents = response.getBody();
        List<Map<String, Object>> allFiles = new ArrayList<>();

        if (contents == null) return allFiles;

        for (Map<String, Object> item : contents) {

            String type = (String) item.get("type");
            String itemPath = (String) item.get("path");

            if ("file".equals(type)) {
                allFiles.add(item);
            } else if ("dir".equals(type)) {
                allFiles.addAll(fetchAllFiles(owner, repo, itemPath));
            }
        }

        return allFiles;
    }

    public String[] extractRepoDetails(String repoUrl) {

        if (repoUrl == null || repoUrl.isEmpty()) {
            throw new IllegalArgumentException("Repository URL cannot be null or empty");
        }

        // Remove trailing slash if present
        repoUrl = repoUrl.trim();
        if (repoUrl.endsWith("/")) {
            repoUrl = repoUrl.substring(0, repoUrl.length() - 1);
        }

        // Basic validation
        if (!repoUrl.contains("github.com")) {
            throw new IllegalArgumentException("Invalid GitHub URL");
        }

        String[] parts = repoUrl.split("/");

        if (parts.length < 5) {
            throw new IllegalArgumentException("Invalid GitHub repository URL format");
        }

        String owner = parts[3];
        String repo = parts[4];

        // Remove .git if present
        if (repo.endsWith(".git")) {
            repo = repo.substring(0, repo.length() - 4);
        }

        return new String[]{owner, repo};
    }
}