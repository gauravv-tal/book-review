package com.bookreview.service;

import com.bookreview.model.Book;
import com.bookreview.model.Favourite;
import com.bookreview.model.User;
import com.bookreview.repository.BookRepository;
import com.bookreview.repository.FavouriteRepository;
import com.bookreview.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FavouriteService {

    private final FavouriteRepository favouriteRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public FavouriteService(FavouriteRepository favouriteRepository, BookRepository bookRepository, UserRepository userRepository) {
        this.favouriteRepository = favouriteRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    public List<Favourite> findByUserId(Long userId) {
        return favouriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public boolean isFavourite(Long userId, Long bookId) {
        return favouriteRepository.existsByUserIdAndBookId(userId, bookId);
    }

    @Transactional
    public Favourite addFavourite(Long userId, Long bookId) {
        // Check if already favourited
        if (favouriteRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new IllegalArgumentException("Book is already favourited by this user");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Favourite favourite = Favourite.builder()
                .user(user)
                .book(book)
                .build();

        return favouriteRepository.save(favourite);
    }

    @Transactional
    public void removeFavourite(Long userId, Long bookId) {
        if (!favouriteRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new IllegalArgumentException("Favourite not found");
        }

        favouriteRepository.deleteByUserIdAndBookId(userId, bookId);
    }

    @Transactional
    public void toggleFavourite(Long userId, Long bookId) {
        if (favouriteRepository.existsByUserIdAndBookId(userId, bookId)) {
            removeFavourite(userId, bookId);
        } else {
            addFavourite(userId, bookId);
        }
    }
}
