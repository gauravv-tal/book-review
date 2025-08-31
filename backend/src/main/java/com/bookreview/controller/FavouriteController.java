package com.bookreview.controller;

import com.bookreview.model.Favourite;
import com.bookreview.service.AuthService;
import com.bookreview.service.FavouriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favourites")
@Tag(name = "Favourites", description = "Book favourite endpoints")
public class FavouriteController {

    private final FavouriteService favouriteService;
    private final AuthService authService;

    public FavouriteController(FavouriteService favouriteService, AuthService authService) {
        this.favouriteService = favouriteService;
        this.authService = authService;
    }

    @Operation(summary = "Get user's favourites")
    @GetMapping("/my")
    public ResponseEntity<List<Favourite>> getMyFavourites(Authentication auth) {
        Long userId = authService.extractUserId(auth);
        return ResponseEntity.ok(favouriteService.findByUserId(userId));
    }

    @Operation(summary = "Check if book is favourited by user")
    @GetMapping("/book/{bookId}/check")
    public ResponseEntity<Boolean> checkFavourite(@PathVariable Long bookId, Authentication auth) {
        Long userId = authService.extractUserId(auth);
        return ResponseEntity.ok(favouriteService.isFavourite(userId, bookId));
    }

    @Operation(summary = "Add book to favourites")
    @PostMapping("/book/{bookId}")
    public ResponseEntity<Favourite> addFavourite(@PathVariable Long bookId, Authentication auth) {
        Long userId = authService.extractUserId(auth);
        Favourite favourite = favouriteService.addFavourite(userId, bookId);
        return ResponseEntity.ok(favourite);
    }

    @Operation(summary = "Remove book from favourites")
    @DeleteMapping("/book/{bookId}")
    public ResponseEntity<Void> removeFavourite(@PathVariable Long bookId, Authentication auth) {
        Long userId = authService.extractUserId(auth);
        favouriteService.removeFavourite(userId, bookId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Toggle favourite status for a book")
    @PutMapping("/book/{bookId}/toggle")
    public ResponseEntity<Void> toggleFavourite(@PathVariable Long bookId, Authentication auth) {
        Long userId = authService.extractUserId(auth);
        favouriteService.toggleFavourite(userId, bookId);
        return ResponseEntity.ok().build();
    }
}
