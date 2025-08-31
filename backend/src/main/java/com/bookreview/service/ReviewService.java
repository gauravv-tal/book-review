package com.bookreview.service;

import com.bookreview.model.Book;
import com.bookreview.model.Review;
import com.bookreview.model.User;
import com.bookreview.repository.BookRepository;
import com.bookreview.repository.ReviewRepository;
import com.bookreview.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository, BookRepository bookRepository, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    public Optional<Review> findByBookIdAndUserId(Long bookId, Long userId) {
        return reviewRepository.findByBookIdAndUserId(bookId, userId);
    }

    public List<Review> findByBookId(Long bookId) {
        return reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId);
    }

    public List<Review> findByUserId(Long userId) {
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public Review createOrUpdateReview(Long bookId, Long userId, String text, Integer rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Optional<Review> existingReview = reviewRepository.findByBookIdAndUserId(bookId, userId);
        
        Review review;
        if (existingReview.isPresent()) {
            // Update existing review
            review = existingReview.get();
            review.setText(text);
            review.setRating(rating.doubleValue());
        } else {
            // Create new review
            review = new Review();
            review.setBook(book);
            review.setUser(user);
            review.setText(text);
            review.setRating(rating.doubleValue());
        }

        review = reviewRepository.save(review);
        updateBookAggregates(bookId);
        return review;
    }

    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        
        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Cannot delete another user's review");
        }

        Long bookId = review.getBook().getId();
        reviewRepository.delete(review);
        updateBookAggregates(bookId);
    }

    private void updateBookAggregates(Long bookId) {
        Double avgRating = reviewRepository.getAverageRatingByBookId(bookId);
        Long reviewCount = reviewRepository.getReviewCountByBookId(bookId);

        Book book = bookRepository.findById(bookId).orElseThrow();
        
        // Round to 1 decimal place
        if (avgRating != null) {
            BigDecimal rounded = BigDecimal.valueOf(avgRating).setScale(1, RoundingMode.HALF_UP);
            book.setAvgRating(rounded.doubleValue());
        } else {
            book.setAvgRating(null);
        }
        
        book.setReviewCount(reviewCount);
        bookRepository.save(book);
    }
}
