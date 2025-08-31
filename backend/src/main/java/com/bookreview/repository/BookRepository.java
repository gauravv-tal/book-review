package com.bookreview.repository;

import com.bookreview.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
    
    @Query("SELECT b FROM Book b WHERE b.avgRating IS NOT NULL ORDER BY b.avgRating DESC, b.reviewCount DESC")
    List<Book> findTopRated(Pageable pageable);
}
