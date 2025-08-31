package com.bookreview.controller;

import com.bookreview.model.Review;
import com.bookreview.service.AuthService;
import com.bookreview.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@Tag(name = "Reviews", description = "Book review endpoints")
public class    ReviewController {

    private final ReviewService reviewService;
    private final AuthService authService;

    public ReviewController(ReviewService reviewService, AuthService authService) {
        this.reviewService = reviewService;
        this.authService = authService;
    }

    @Operation(summary = "Get reviews for a book")
    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<Review>> getReviewsByBook(@PathVariable Long bookId) {
        return ResponseEntity.ok(reviewService.findByBookId(bookId));
    }

    @Operation(summary = "Get user's own reviews")
    @GetMapping("/my")
    public ResponseEntity<List<Review>> getMyReviews(Authentication auth) {
        Long userId = authService.extractUserId(auth);
        return ResponseEntity.ok(reviewService.findByUserId(userId));
    }

    @Operation(summary = "Get user's review for a specific book")
    @GetMapping("/book/{bookId}/my")
    public ResponseEntity<Review> getMyReviewForBook(@PathVariable Long bookId, Authentication auth) {
        Long userId = authService.extractUserId(auth);
        return reviewService.findByBookIdAndUserId(bookId, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create or update review for a book")
    @PostMapping("/book/{bookId}")
    public ResponseEntity<Review> createOrUpdateReview(
            @PathVariable Long bookId,
            @RequestBody ReviewRequest request,
            Authentication auth) {
        Long userId = authService.extractUserId(auth);
        
        Review review = reviewService.createOrUpdateReview(
                bookId, userId, request.text(), request.rating());
        return ResponseEntity.ok(review);
    }

    @Operation(summary = "Delete own review")
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId, Authentication auth) {
        Long userId = authService.extractUserId(auth);
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.noContent().build();
    }

    public record ReviewRequest(String text, Integer rating) {}
}
