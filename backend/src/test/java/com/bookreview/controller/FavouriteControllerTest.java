package com.bookreview.controller;

import com.bookreview.model.Favourite;
import com.bookreview.service.AuthService;
import com.bookreview.service.FavouriteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class FavouriteControllerTest {

    @Mock
    private FavouriteService favouriteService;

    @Mock
    private AuthService authService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private FavouriteController favouriteController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(authService.extractUserId(any())).thenReturn(1L);
    }

    @Test
    void getMyFavourites_shouldReturnFavourites() {
        // Arrange
        Favourite favourite1 = Favourite.builder()
            .id(1L)
            .build();
        
        Favourite favourite2 = Favourite.builder()
            .id(2L)
            .build();

        List<Favourite> expectedFavourites = Arrays.asList(favourite1, favourite2);
        when(favouriteService.findByUserId(1L)).thenReturn(expectedFavourites);

        // Act
        ResponseEntity<List<Favourite>> response = favouriteController.getMyFavourites(authentication);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
        verify(favouriteService).findByUserId(1L);
    }

    @Test
    void checkFavourite_shouldReturnTrueWhenFavourited() {
        // Arrange
        when(favouriteService.isFavourite(1L, 1L)).thenReturn(true);

        // Act
        ResponseEntity<Boolean> response = favouriteController.checkFavourite(1L, authentication);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody());
        verify(favouriteService).isFavourite(1L, 1L);
    }

    @Test
    void addFavourite_shouldReturnCreatedFavourite() {
        // Arrange
        Favourite expectedFavourite = Favourite.builder()
            .id(1L)
            .build();
        when(favouriteService.addFavourite(1L, 1L)).thenReturn(expectedFavourite);

        // Act
        ResponseEntity<Favourite> response = favouriteController.addFavourite(1L, authentication);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedFavourite, response.getBody());
        verify(favouriteService).addFavourite(1L, 1L);
    }

    @Test
    void removeFavourite_shouldCallService() {
        // Act
        ResponseEntity<Void> response = favouriteController.removeFavourite(1L, authentication);

        // Assert
        assertNotNull(response);
        assertEquals(204, response.getStatusCodeValue());
        verify(favouriteService).removeFavourite(1L, 1L);
    }

    @Test
    void toggleFavourite_shouldCallService() {
        // Act
        ResponseEntity<Void> response = favouriteController.toggleFavourite(1L, authentication);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(favouriteService).toggleFavourite(1L, 1L);
    }
}
