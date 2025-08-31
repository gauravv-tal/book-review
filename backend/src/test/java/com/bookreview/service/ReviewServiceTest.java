package com.bookreview.service;

import com.bookreview.model.Book;
import com.bookreview.model.Review;
import com.bookreview.model.User;
import com.bookreview.repository.BookRepository;
import com.bookreview.repository.ReviewRepository;
import com.bookreview.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReviewServiceTest {
    private ReviewRepository reviewRepository;
    private BookRepository bookRepository;
    private UserRepository userRepository;
    private ReviewService service;

    @BeforeEach
    void setup() {
        reviewRepository = mock(ReviewRepository.class);
        bookRepository = mock(BookRepository.class);
        userRepository = mock(UserRepository.class);
        service = new ReviewService(reviewRepository, bookRepository, userRepository);
    }

    @Test
    void finders_delegate() {
        when(reviewRepository.findByBookIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());
        when(reviewRepository.findByUserIdOrderByCreatedAtDesc(2L)).thenReturn(List.of());
        when(reviewRepository.findByBookIdAndUserId(1L, 2L)).thenReturn(Optional.empty());
        assertNotNull(service.findByBookId(1L));
        assertNotNull(service.findByUserId(2L));
        assertTrue(service.findByBookIdAndUserId(1L, 2L).isEmpty());
    }

    @Test
    void createOrUpdateReview_creates_whenMissing_andUpdatesAggregates() {
        Book book = new Book(); book.setId(10L);
        User user = new User(); user.setId(5L);
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(reviewRepository.findByBookIdAndUserId(10L, 5L)).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> {
            Review r = inv.getArgument(0);
            r.setId(99L);
            return r;
        });
        when(reviewRepository.getAverageRatingByBookId(10L)).thenReturn(4.26);
        when(reviewRepository.getReviewCountByBookId(10L)).thenReturn(3L);
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));

        Review r = service.createOrUpdateReview(10L, 5L, "Great book", 5);
        assertEquals(99L, r.getId());
        verify(reviewRepository).save(any(Review.class));
        verify(bookRepository, atLeastOnce()).save(any(Book.class));
        assertEquals(4.3, book.getAvgRating()); // rounded to 1 dp
        assertEquals(3L, book.getReviewCount());
    }

    @Test
    void createOrUpdateReview_updates_whenExisting() {
        Book book = new Book(); book.setId(10L);
        User user = new User(); user.setId(5L);
        Review existing = new Review(); existing.setId(50L); existing.setBook(book); existing.setUser(user);
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(reviewRepository.findByBookIdAndUserId(10L, 5L)).thenReturn(Optional.of(existing));
        when(reviewRepository.save(existing)).thenReturn(existing);
        when(reviewRepository.getAverageRatingByBookId(10L)).thenReturn(null);
        when(reviewRepository.getReviewCountByBookId(10L)).thenReturn(0L);

        Review r = service.createOrUpdateReview(10L, 5L, "Updated", 3);
        assertEquals(50L, r.getId());
        assertEquals(3.0, r.getRating());
        assertNull(book.getAvgRating());
        assertEquals(0L, book.getReviewCount());
    }

    @Test
    void createOrUpdateReview_invalidRating_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.createOrUpdateReview(1L, 1L, "x", 0));
        assertThrows(IllegalArgumentException.class, () -> service.createOrUpdateReview(1L, 1L, "x", 6));
    }

    @Test
    void deleteReview_checksOwnership_andUpdatesAggregates() {
        Book book = new Book(); book.setId(10L);
        User owner = new User(); owner.setId(5L);
        Review review = new Review(); review.setId(77L); review.setBook(book); review.setUser(owner);
        when(reviewRepository.findById(77L)).thenReturn(Optional.of(review));
        when(reviewRepository.getAverageRatingByBookId(10L)).thenReturn(4.0);
        when(reviewRepository.getReviewCountByBookId(10L)).thenReturn(1L);
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));

        service.deleteReview(77L, 5L);
        verify(reviewRepository).delete(review);
        verify(bookRepository).save(book);
    }

    @Test
    void deleteReview_notOwner_throws() {
        Book book = new Book(); book.setId(10L);
        User other = new User(); other.setId(9L);
        Review review = new Review(); review.setId(77L); review.setBook(book); review.setUser(other);
        when(reviewRepository.findById(77L)).thenReturn(Optional.of(review));
        assertThrows(IllegalArgumentException.class, () -> service.deleteReview(77L, 5L));
    }
}
