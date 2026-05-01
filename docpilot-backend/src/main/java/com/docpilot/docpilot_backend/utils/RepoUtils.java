package com.docpilot.docpilot_backend.utils;

public class RepoUtils {

    public static boolean isValidGitHubUrl(String url) {
        return url != null && url.contains("github.com");
    }
}
