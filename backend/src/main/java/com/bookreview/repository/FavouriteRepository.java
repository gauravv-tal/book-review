package com.bookreview.repository;

import com.bookreview.model.Favourite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavouriteRepository extends JpaRepository<Favourite, Long> {

    Optional<Favourite> findByUserIdAndBookId(Long userId, Long bookId);

    List<Favourite> findByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByUserIdAndBookId(Long userId, Long bookId);

    void deleteByUserIdAndBookId(Long userId, Long bookId);
}
