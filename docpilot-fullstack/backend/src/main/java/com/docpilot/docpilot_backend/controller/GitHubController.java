package com.docpilot.docpilot_backend.controller;

import com.docpilot.docpilot_backend.dto.CurrentUserResponse;
import com.docpilot.docpilot_backend.model.GitHubRepository;
import com.docpilot.docpilot_backend.service.GitHubOAuthService;
import com.docpilot.docpilot_backend.service.GitHubService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/github")
public class GitHubController {

    private final GitHubOAuthService oauthService;
    private final GitHubService gitHubService;

    @Value("${github.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public GitHubController(
            GitHubOAuthService oauthService,
            GitHubService gitHubService) {
        this.oauthService = oauthService;
        this.gitHubService = gitHubService;
    }

    @GetMapping("/login")
    public ResponseEntity<Void> login(HttpSession session) {
        URI authorizationUri =
                oauthService.buildAuthorizationUri(session);

        return ResponseEntity.status(302)
                .location(authorizationUri)
                .build();
    }

    @GetMapping("/callback")
    public RedirectView callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            HttpSession session) {

        if (error != null) {
            return new RedirectView(
                    frontendUrl + "/?error=github_denied"
            );
        }

        try {
            if (code == null || state == null) {
                throw new IllegalArgumentException("Missing OAuth code or state.");
            }

            oauthService.handleCallback(code, state, session);

            return new RedirectView(
                    frontendUrl + "/?connected=true"
            );

        } catch (Exception e) {
            return new RedirectView(
                    frontendUrl + "/?error=github_auth_failed"
            );
        }
    }

    @GetMapping("/me")
    public CurrentUserResponse me(HttpSession session) {
        Map<String, Object> user = oauthService.getUser(session);

        if (user == null) {
            return new CurrentUserResponse(false, null, null, null);
        }

        return new CurrentUserResponse(
                true,
                value(user, "login"),
                value(user, "name"),
                value(user, "avatar_url")
        );
    }

    @GetMapping("/repositories")
    public ResponseEntity<List<GitHubRepository>> repositories(
            HttpSession session) {

        String token = oauthService.getAccessToken(session);

        if (token == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(
                gitHubService.getRepositories(token)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        oauthService.logout(session);
        return ResponseEntity.noContent().build();
    }

    private String value(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? null : value.toString();
    }
}
