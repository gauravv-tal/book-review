package com.bookreview.service;

import com.bookreview.model.Book;
import com.bookreview.model.Favourite;
import com.bookreview.model.User;
import com.bookreview.repository.BookRepository;
import com.bookreview.repository.FavouriteRepository;
import com.bookreview.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FavouriteServiceTest {
    private FavouriteRepository favouriteRepository;
    private BookRepository bookRepository;
    private UserRepository userRepository;
    private FavouriteService service;

    @BeforeEach
    void setup() {
        favouriteRepository = mock(FavouriteRepository.class);
        bookRepository = mock(BookRepository.class);
        userRepository = mock(UserRepository.class);
        service = new FavouriteService(favouriteRepository, bookRepository, userRepository);
    }

    @Test
    void findByUserId_delegates() {
        when(favouriteRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());
        assertNotNull(service.findByUserId(1L));
        verify(favouriteRepository).findByUserIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void addFavourite_success() {
        when(favouriteRepository.existsByUserIdAndBookId(1L, 2L)).thenReturn(false);
        Book b = new Book(); b.setId(2L);
        User u = new User(); u.setId(1L);
        when(bookRepository.findById(2L)).thenReturn(Optional.of(b));
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(favouriteRepository.save(any(Favourite.class))).thenAnswer(inv -> inv.getArgument(0));

        Favourite f = service.addFavourite(1L, 2L);
        assertEquals(2L, f.getBook().getId());
        assertEquals(1L, f.getUser().getId());
    }

    @Test
    void addFavourite_duplicate_throws() {
        when(favouriteRepository.existsByUserIdAndBookId(1L, 2L)).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.addFavourite(1L, 2L));
    }

    @Test
    void removeFavourite_success() {
        when(favouriteRepository.existsByUserIdAndBookId(1L, 2L)).thenReturn(true);
        service.removeFavourite(1L, 2L);
        verify(favouriteRepository).deleteByUserIdAndBookId(1L, 2L);
    }

    @Test
    void removeFavourite_missing_throws() {
        when(favouriteRepository.existsByUserIdAndBookId(1L, 2L)).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> service.removeFavourite(1L, 2L));
    }

    @Test
    void toggleFavourite_addsWhenMissing_andRemovesWhenExists() {
        Book b = new Book(); b.setId(2L);
        User u = new User(); u.setId(1L);
        when(bookRepository.findById(2L)).thenReturn(Optional.of(b));
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(favouriteRepository.save(any(Favourite.class))).thenAnswer(inv -> inv.getArgument(0));

        when(favouriteRepository.existsByUserIdAndBookId(1L, 2L)).thenReturn(false);

        service.toggleFavourite(1L, 2L); // adds

        verify(favouriteRepository).save(any(Favourite.class));

        when(favouriteRepository.existsByUserIdAndBookId(1L, 2L)).thenReturn(true);

        service.toggleFavourite(1L, 2L); // removes
        verify(favouriteRepository).deleteByUserIdAndBookId(1L, 2L);
    }
}
