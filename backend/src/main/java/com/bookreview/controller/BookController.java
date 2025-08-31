package com.bookreview.controller;

import com.bookreview.model.Book;
import com.bookreview.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping
@Tag(name = "Books", description = "Books catalog endpoints")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @Operation(summary = "Search/list books", description = "Filter by optional title, author, genre, year with pagination")
    @GetMapping("/books")
    public ResponseEntity<Page<Book>> searchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) Integer year,
            @Parameter(hidden = true) @PageableDefault(size = 20, sort = {"title"}) Pageable pageable) {
        return ResponseEntity.ok(bookService.search(title, author, genre, year, pageable));
    }

    @Operation(summary = "Get book details by ID")
    @GetMapping("/books/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return bookService
                .findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Import books via CSV (admin-only)",
            description = "Accepts a CSV file with headers: title, author, description, cover_url, genres, year",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Import result",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ImportResponse.class)))
            })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/admin/books/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResponse> importBooks(@RequestPart("file") MultipartFile file) throws Exception {
        int count = bookService.importCsv(file);
        return ResponseEntity.ok(new ImportResponse(count));
    }

    // Simple response DTO for import endpoint
    public record ImportResponse(int imported) { }
}
