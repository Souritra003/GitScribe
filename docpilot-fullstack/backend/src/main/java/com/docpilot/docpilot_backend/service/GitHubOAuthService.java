package com.docpilot.docpilot_backend.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
public class GitHubOAuthService {

    private static final String STATE_KEY = "github_oauth_state";
    private static final String VERIFIER_KEY = "github_oauth_verifier";
    private static final String TOKEN_KEY = "github_access_token";
    private static final String USER_KEY = "github_user";

    @Value("${github.client-id}")
    private String clientId;

    @Value("${github.client-secret}")
    private String clientSecret;

    @Value("${github.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate;

    public GitHubOAuthService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public URI buildAuthorizationUri(HttpSession session) {
        String state = randomUrlSafe(32);
        String verifier = randomUrlSafe(64);

        session.setAttribute(STATE_KEY, state);
        session.setAttribute(VERIFIER_KEY, verifier);

        String challenge = createCodeChallenge(verifier);

        return UriComponentsBuilder
                .fromUriString("https://github.com/login/oauth/authorize")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", "read:user repo")
                .queryParam("state", state)
                .queryParam("code_challenge", challenge)
                .queryParam("code_challenge_method", "S256")
                .build()
                .toUri();
    }

    public void handleCallback(String code, String state, HttpSession session) {
        String expectedState = (String) session.getAttribute(STATE_KEY);
        String verifier = (String) session.getAttribute(VERIFIER_KEY);

        if (expectedState == null || !MessageDigest.isEqual(
                expectedState.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                (state == null ? "" : state).getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
            throw new IllegalArgumentException("Invalid OAuth state.");
        }

        if (verifier == null) {
            throw new IllegalArgumentException("OAuth verifier is missing.");
        }

        session.removeAttribute(STATE_KEY);
        session.removeAttribute(VERIFIER_KEY);

        String token = exchangeCodeForToken(code, verifier);
        session.setAttribute(TOKEN_KEY, token);

        Map<String, Object> user = getAuthenticatedUser(token);
        session.setAttribute(USER_KEY, user);
    }

    private String exchangeCodeForToken(String code, String verifier) {
        String url = "https://github.com/login/oauth/access_token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("code", code);
        form.add("redirect_uri", redirectUri);
        form.add("code_verifier", verifier);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(form, headers),
                Map.class
        );

        Map body = response.getBody();
        if (body == null || body.get("access_token") == null) {
            throw new IllegalStateException("GitHub did not return an access token.");
        }

        return body.get("access_token").toString();
    }

    private Map<String, Object> getAuthenticatedUser(String token) {
        HttpHeaders headers = githubHeaders(token);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://api.github.com/user",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        if (response.getBody() == null) {
            throw new IllegalStateException("Unable to retrieve GitHub user.");
        }

        return response.getBody();
    }

    public String getAccessToken(HttpSession session) {
        Object token = session.getAttribute(TOKEN_KEY);
        return token == null ? null : token.toString();
    }

    public Map<String, Object> getUser(HttpSession session) {
        Object user = session.getAttribute(USER_KEY);
        if (user instanceof Map<?, ?> map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> typed = (Map<String, Object>) map;
            return typed;
        }
        return null;
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }

    public HttpHeaders githubHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.set("Accept", "application/vnd.github+json");
        headers.set("X-GitHub-Api-Version", "2026-03-10");
        return headers;
    }

    private String randomUrlSafe(int bytes) {
        byte[] value = new byte[bytes];
        new SecureRandom().nextBytes(value);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private String createCodeChallenge(String verifier) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(verifier.getBytes(java.nio.charset.StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create PKCE challenge.", e);
        }
    }
}
