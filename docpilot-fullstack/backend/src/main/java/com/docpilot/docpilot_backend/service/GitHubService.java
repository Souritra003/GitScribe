package com.docpilot.docpilot_backend.service;

import com.docpilot.docpilot_backend.model.GitHubRepository;
import com.docpilot.docpilot_backend.model.RepositorySnapshot;
import com.docpilot.docpilot_backend.model.SourceFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class GitHubService {

    private final RestTemplate restTemplate;
    private final GitHubOAuthService oauthService;

    @Value("${docpilot.max-files:150}")
    private int maxFiles;

    @Value("${docpilot.max-file-size-bytes:200000}")
    private long maxFileSizeBytes;

    private static final Set<String> TEXT_EXTENSIONS = Set.of(
            "java", "kt", "kts", "groovy", "scala", "py", "js", "jsx", "ts", "tsx",
            "c", "h", "cpp", "cc", "hpp", "cs", "go", "rs", "php", "rb", "swift",
            "html", "css", "scss", "sass", "vue", "sql", "sh", "bash", "zsh",
            "xml", "yaml", "yml", "json", "toml", "properties", "md", "txt",
            "dockerfile", "gradle", "proto"
    );

    private static final Set<String> IGNORED_PARTS = Set.of(
            ".git", "node_modules", "target", "build", "dist", "out",
            ".idea", ".vscode", "coverage", "__pycache__", ".next", "vendor"
    );

    public GitHubService(RestTemplate restTemplate, GitHubOAuthService oauthService) {
        this.restTemplate = restTemplate;
        this.oauthService = oauthService;
    }

    public List<GitHubRepository> getRepositories(String token) {
        List<GitHubRepository> result = new ArrayList<>();

        for (int page = 1; page <= 10; page++) {
            String url = UriComponentsBuilder
                    .fromUriString("https://api.github.com/user/repos")
                    .queryParam("per_page", 100)
                    .queryParam("page", page)
                    .queryParam("sort", "updated")
                    .build()
                    .toUriString();

            ResponseEntity<List> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(oauthService.githubHeaders(token)),
                    List.class
            );

            List<Map<String, Object>> body = response.getBody();
            if (body == null || body.isEmpty()) break;

            for (Map<String, Object> repo : body) {
                GitHubRepository item = new GitHubRepository();
                item.setName(stringValue(repo.get("name")));
                item.setFullName(stringValue(repo.get("full_name")));
                item.setHtmlUrl(stringValue(repo.get("html_url")));
                item.setDescription(stringValue(repo.get("description")));
                item.setDefaultBranch(stringValue(repo.get("default_branch")));
                item.setPrivateRepository(Boolean.TRUE.equals(repo.get("private")));
                result.add(item);
            }

            if (body.size() < 100) break;
        }

        return result;
    }

    public RepositorySnapshot fetchRepository(String owner, String repo, String token) {
        Map<String, Object> metadata = getRepositoryMetadata(owner, repo, token);
        String defaultBranch = stringValue(metadata.get("default_branch"));

        String treeUrl = "https://api.github.com/repos/" + owner + "/" + repo
                + "/git/trees/" + defaultBranch + "?recursive=1";

        ResponseEntity<Map> treeResponse = restTemplate.exchange(
                treeUrl,
                HttpMethod.GET,
                new HttpEntity<>(oauthService.githubHeaders(token)),
                Map.class
        );

        Map treeBody = treeResponse.getBody();
        if (treeBody == null) {
            throw new IllegalStateException("GitHub returned no repository tree.");
        }

        Object truncated = treeBody.get("truncated");
        if (Boolean.TRUE.equals(truncated)) {
            throw new IllegalStateException(
                    "Repository is too large for this MVP tree scan. " +
                    "Add a paginated/non-recursive tree scanner for very large repositories."
            );
        }

        List<Map<String, Object>> tree =
                (List<Map<String, Object>>) treeBody.getOrDefault("tree", List.of());

        List<String> allPaths = new ArrayList<>();
        List<SourceFile> sourceFiles = new ArrayList<>();

        for (Map<String, Object> item : tree) {
            if (!"blob".equals(item.get("type"))) continue;

            String path = stringValue(item.get("path"));
            long size = numberValue(item.get("size"));

            allPaths.add(path);

            if (shouldIgnore(path) || !isLikelyTextFile(path)) continue;
            if (size > maxFileSizeBytes) continue;
            if (sourceFiles.size() >= maxFiles) continue;

            String sha = stringValue(item.get("sha"));
            String content = getBlobContent(owner, repo, sha, token);

            if (content == null || content.isBlank()) continue;

            sourceFiles.add(new SourceFile(
                    path,
                    detectLanguage(path),
                    size,
                    content
            ));
        }

        return new RepositorySnapshot(
                owner,
                stringValue(metadata.get("name")),
                stringValue(metadata.get("full_name")),
                stringValue(metadata.get("description")),
                defaultBranch,
                stringValue(metadata.get("html_url")),
                allPaths,
                sourceFiles
        );
    }

    private Map<String, Object> getRepositoryMetadata(String owner, String repo, String token) {
        String url = "https://api.github.com/repos/" + owner + "/" + repo;

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(oauthService.githubHeaders(token)),
                Map.class
        );

        if (response.getBody() == null) {
            throw new IllegalStateException("Repository not found or inaccessible.");
        }

        return response.getBody();
    }

    private String getBlobContent(String owner, String repo, String sha, String token) {
        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/git/blobs/" + sha;

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(oauthService.githubHeaders(token)),
                Map.class
        );

        Map body = response.getBody();
        if (body == null) return null;

        String encoding = stringValue(body.get("encoding"));
        String content = stringValue(body.get("content"));

        if (!"base64".equalsIgnoreCase(encoding) || content == null) {
            return null;
        }

        try {
            return new String(
                    Base64.getMimeDecoder().decode(content),
                    StandardCharsets.UTF_8
            );
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private boolean shouldIgnore(String path) {
        String normalized = path.replace('\\', '/');
        for (String part : normalized.split("/")) {
            if (IGNORED_PARTS.contains(part)) return true;
        }

        String lower = normalized.toLowerCase(Locale.ROOT);
        return lower.endsWith(".min.js")
                || lower.endsWith(".map")
                || lower.endsWith(".lock");
    }

    private boolean isLikelyTextFile(String path) {
        String fileName = path.substring(path.lastIndexOf('/') + 1).toLowerCase(Locale.ROOT);

        if (fileName.equals("dockerfile")
                || fileName.equals("makefile")
                || fileName.equals("procfile")
                || fileName.equals(".gitignore")) {
            return true;
        }

        int dot = fileName.lastIndexOf('.');
        if (dot < 0) return false;

        return TEXT_EXTENSIONS.contains(fileName.substring(dot + 1));
    }

    private String detectLanguage(String path) {
        String fileName = path.substring(path.lastIndexOf('/') + 1).toLowerCase(Locale.ROOT);

        if (fileName.equals("dockerfile")) return "Dockerfile";

        int dot = fileName.lastIndexOf('.');
        if (dot < 0) return "Unknown";

        return switch (fileName.substring(dot + 1)) {
            case "java" -> "Java";
            case "kt", "kts" -> "Kotlin";
            case "groovy" -> "Groovy";
            case "scala" -> "Scala";
            case "py" -> "Python";
            case "js", "jsx" -> "JavaScript";
            case "ts", "tsx" -> "TypeScript";
            case "c", "h" -> "C";
            case "cpp", "cc", "hpp" -> "C++";
            case "cs" -> "C#";
            case "go" -> "Go";
            case "rs" -> "Rust";
            case "php" -> "PHP";
            case "rb" -> "Ruby";
            case "swift" -> "Swift";
            case "html" -> "HTML";
            case "css", "scss", "sass" -> "CSS";
            case "vue" -> "Vue";
            case "sql" -> "SQL";
            case "xml" -> "XML";
            case "yaml", "yml" -> "YAML";
            case "json" -> "JSON";
            case "toml" -> "TOML";
            case "properties" -> "Properties";
            case "md" -> "Markdown";
            case "sh", "bash", "zsh" -> "Shell";
            case "gradle" -> "Gradle";
            case "proto" -> "Protocol Buffers";
            default -> "Other";
        };
    }

    private String stringValue(Object value) {
        return value == null ? "" : value.toString();
    }

    private long numberValue(Object value) {
        return value instanceof Number n ? n.longValue() : 0L;
    }
}
