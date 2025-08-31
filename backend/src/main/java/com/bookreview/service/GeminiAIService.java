package com.bookreview.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class GeminiAIService {
    private final String geminiApiKey;


    public GeminiAIService(
            @Value("${GEMINI_API_KEY}") String geminiApiKey) {
        this.geminiApiKey = geminiApiKey;
    }

    public String generateContent(String prompt) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + geminiApiKey;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("contents", List.of(
                Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", prompt))
                )
        ));

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        var response = restTemplate.postForEntity(url, request, String.class);
        return Optional.ofNullable(response.getBody()).orElse("");
    }

    public String extractTextFromResponse(String jsonResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(jsonResponse);
            return parseResponse(root).toString().trim();
        } catch (Exception e) {
            return "";
        }
    }

    public String extractCleanJsonFromResponse(String jsonResponse) {
        String content = extractTextFromResponse(jsonResponse);
        return cleanJsonContent(content);
    }

    private String cleanJsonContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return content;
        }
        
        // Remove markdown code block markers
        String cleaned = content.trim();
        
        // Remove ```json at the beginning
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7).trim();
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3).trim();
        }
        
        // Remove ``` at the end
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3).trim();
        }
        
        return cleaned;
    }

    private StringBuilder parseResponse(JsonNode root) {
        StringBuilder contentSb = new StringBuilder();
        if (root.has("candidates") && root.get("candidates").isArray() && root.get("candidates").size() > 0) {
            JsonNode contentNode = root.get("candidates").get(0).get("content");
            if (contentNode != null && contentNode.has("parts") && contentNode.get("parts").isArray()) {
                for (JsonNode part : contentNode.get("parts")) {
                    if (part.has("text")) {
                        contentSb.append(part.get("text").asText(""));
                        contentSb.append("\n");
                    }
                }
            }
        }
        return contentSb;
    }
}
