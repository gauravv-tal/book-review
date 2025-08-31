package com.bookreview.controller;

import com.bookreview.model.Review;
import com.bookreview.service.AuthService;
import com.bookreview.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    @Mock
    private AuthService authService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ReviewController reviewController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(authService.extractUserId(any())).thenReturn(1L);
    }

    @Test
    void getReviewsByBook_shouldReturnReviews() {
        // Arrange
        Review review1 = new Review();
        review1.setId(1L);
        review1.setText("Great book!");
        review1.setRating(5.0);

        Review review2 = new Review();
        review2.setId(2L);
        review2.setText("Good read");
        review2.setRating(4.0);

        List<Review> expectedReviews = Arrays.asList(review1, review2);
        when(reviewService.findByBookId(1L)).thenReturn(expectedReviews);

        // Act
        ResponseEntity<List<Review>> response = reviewController.getReviewsByBook(1L);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
        verify(reviewService).findByBookId(1L);
    }

    @Test
    void getMyReviews_shouldReturnUsersReviews() {
        // Arrange
        Review review = new Review();
        review.setId(1L);
        review.setText("Great book!");
        review.setRating(5.0);

        List<Review> expectedReviews = List.of(review);
        when(reviewService.findByUserId(1L)).thenReturn(expectedReviews);

        // Act
        ResponseEntity<List<Review>> response = reviewController.getMyReviews(authentication);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        verify(reviewService).findByUserId(1L);
    }

    @Test
    void getMyReviewForBook_shouldReturnReviewWhenExists() {
        // Arrange
        Review expectedReview = new Review();
        expectedReview.setId(1L);
        expectedReview.setText("Great book!");
        expectedReview.setRating(5.0);
        
        when(reviewService.findByBookIdAndUserId(1L, 1L)).thenReturn(Optional.of(expectedReview));

        // Act
        ResponseEntity<Review> response = reviewController.getMyReviewForBook(1L, authentication);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedReview, response.getBody());
    }

    @Test
    void getMyReviewForBook_shouldReturnNotFoundWhenNotExists() {
        // Arrange
        when(reviewService.findByBookIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Review> response = reviewController.getMyReviewForBook(999L, authentication);

        // Assert
        assertNotNull(response);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void createOrUpdateReview_shouldCreateNewReview() {
        // Arrange
        Review expectedReview = new Review();
        expectedReview.setId(1L);
        expectedReview.setText("Great book!");
        expectedReview.setRating(5.0);
        
        ReviewController.ReviewRequest request = new ReviewController.ReviewRequest("Great book!", 5);
        when(reviewService.createOrUpdateReview(1L, 1L, "Great book!", 5)).thenReturn(expectedReview);

        // Act
        ResponseEntity<Review> response = reviewController.createOrUpdateReview(1L, request, authentication);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedReview, response.getBody());
        verify(reviewService).createOrUpdateReview(1L, 1L, "Great book!", 5);
    }

    @Test
    void deleteReview_shouldCallService() {
        // Act
        ResponseEntity<Void> response = reviewController.deleteReview(1L, authentication);

        // Assert
        assertNotNull(response);
        assertEquals(204, response.getStatusCodeValue());
        verify(reviewService).deleteReview(1L, 1L);
    }
}
