package com.bookreview.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeminiAIServiceTest {


    private GeminiAIService geminiAIService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String TEST_API_KEY = "test-api-key";

    @BeforeEach
    void setUp() {
        geminiAIService = new GeminiAIService(TEST_API_KEY);
    }


    @Test
    void extractTextFromResponse_validJson_returnsTextContent() throws Exception {
        // Given
        String jsonResponse = """
        {
            "candidates": [
                {
                    "content": {
                        "parts": [
                            {
                                "text": "Test response"
                            }
                        ]
                    }
                }
            ]
        }
        """;
        
        // When
        String result = geminiAIService.extractTextFromResponse(jsonResponse);
        
        // Then
        assertEquals("Test response", result);
    }

    @Test
    void extractTextFromResponse_invalidJson_returnsEmptyString() {
        // Given
        String invalidJson = "{invalid-json";
        
        // When
        String result = geminiAIService.extractTextFromResponse(invalidJson);
        
        // Then
        assertEquals("", result);
    }

    @Test
    void extractTextFromResponse_emptyResponse_returnsEmptyString() {
        // Given
        String emptyJson = "{}";
        
        // When
        String result = geminiAIService.extractTextFromResponse(emptyJson);
        
        // Then
        assertEquals("", result);
    }
}
