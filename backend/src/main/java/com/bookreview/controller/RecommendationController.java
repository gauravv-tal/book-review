package com.bookreview.controller;

import com.bookreview.model.Book;
import com.bookreview.dto.AiRecommendationDto;
import com.bookreview.service.AuthService;
import com.bookreview.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/recommendations")
@Tag(name = "Recommendations", description = "Book recommendation endpoints")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final AuthService authService;

    public RecommendationController(RecommendationService recommendationService, AuthService authService) {
        this.recommendationService = recommendationService;
        this.authService = authService;
    }

    @Operation(summary = "Top-rated books (local)")
    @GetMapping("/top-rated")
    public ResponseEntity<List<Book>> topRated() {
        return ResponseEntity.ok(recommendationService.getTopRated(5));
    }

    @Operation(summary = "AI-based recommendations using user's favourite genres (MVP)")
    @GetMapping("/ai")
    public ResponseEntity<List<AiRecommendationDto>> aiRecommendations(Authentication auth) {
        Long userId = authService.extractUserId(auth);
        return ResponseEntity.ok(recommendationService.getAiRecommendations(userId, 5));
    }
}
