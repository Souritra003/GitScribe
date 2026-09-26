package com.docpilot.docpilot_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class AIService {

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model:gemini-3.8-flash}")
    private String model;

    private final RestTemplate restTemplate;

    public AIService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String generateDocumentation(String prompt) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent?key=" + apiKey;

        Map<String, Object> part = Map.of("text", prompt);
        Map<String, Object> content = Map.of(
                "role", "user",
                "parts", List.of(part)
        );

        Map<String, Object> body = Map.of(
                "contents", List.of(content),
                "generationConfig", Map.of(
                        "temperature", 0.2
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Map.class
        );

        return extractText(response.getBody());
    }

    private String extractText(Map response) {
        if (response == null) {
            throw new IllegalStateException("Gemini returned an empty response.");
        }

        try {
            List<Map<String, Object>> candidates =
                    (List<Map<String, Object>>) response.get("candidates");

            if (candidates == null || candidates.isEmpty()) {
                throw new IllegalStateException("Gemini returned no candidates.");
            }

            Map<String, Object> candidate = candidates.get(0);
            Map<String, Object> content =
                    (Map<String, Object>) candidate.get("content");

            List<Map<String, Object>> parts =
                    (List<Map<String, Object>>) content.get("parts");

            if (parts == null || parts.isEmpty()) {
                throw new IllegalStateException("Gemini returned no text parts.");
            }

            return String.valueOf(parts.get(0).get("text"));

        } catch (ClassCastException | NullPointerException e) {
            throw new IllegalStateException(
                    "Unable to parse Gemini response: " + response,
                    e
            );
        }
    }
}
