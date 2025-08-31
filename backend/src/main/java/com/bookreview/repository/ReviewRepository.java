package com.bookreview.repository;

import com.bookreview.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    Optional<Review> findByBookIdAndUserId(Long bookId, Long userId);
    
    List<Review> findByBookIdOrderByCreatedAtDesc(Long bookId);
    
    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book.id = :bookId")
    Double getAverageRatingByBookId(@Param("bookId") Long bookId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.book.id = :bookId")
    Long getReviewCountByBookId(@Param("bookId") Long bookId);
}
