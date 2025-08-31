package com.bookreview.controller;

import com.bookreview.model.Book;
import com.bookreview.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BookControllerTest {

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController bookController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void searchBooks_shouldReturnPageOfBooks() {
        // Arrange
        Book book1 = new Book();
        book1.setId(1L);
        book1.setTitle("Book 1");
        book1.setAuthor("Author 1");
        book1.setDescription("Description 1");
        book1.setCoverUrl("url1");
        book1.setGenres("Fiction");
        book1.setYear(2020);

        Book book2 = new Book();
        book2.setId(2L);
        book2.setTitle("Book 2");
        book2.setAuthor("Author 2");
        book2.setDescription("Description 2");
        book2.setCoverUrl("url2");
        book2.setGenres("Non-Fiction");
        book2.setYear(2021);

        Page<Book> expectedPage = new PageImpl<>(List.of(book1, book2));
        when(bookService.search(any(), any(), any(), any(), any(Pageable.class))).thenReturn(expectedPage);

        // Act
        ResponseEntity<Page<Book>> response = bookController.searchBooks("book", "author", "Fiction", 2020, Pageable.unpaged());

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().getTotalElements());
        verify(bookService).search("book", "author", "Fiction", 2020, Pageable.unpaged());
    }

    @Test
    void getBookById_shouldReturnBookWhenFound() {
        // Arrange
        Book expectedBook = new Book();
        expectedBook.setId(1L);
        expectedBook.setTitle("Book 1");
        expectedBook.setAuthor("Author 1");
        expectedBook.setDescription("Description 1");
        expectedBook.setCoverUrl("url1");
        expectedBook.setGenres("Fiction");
        expectedBook.setYear(2020);
        
        when(bookService.findById(1L)).thenReturn(Optional.of(expectedBook));

        // Act
        ResponseEntity<Book> response = bookController.getBookById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedBook, response.getBody());
    }

    @Test
    void getBookById_shouldReturnNotFoundWhenBookNotExists() {
        // Arrange
        when(bookService.findById(999L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Book> response = bookController.getBookById(999L);

        // Assert
        assertNotNull(response);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void importBooks_shouldReturnImportResult() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "books.csv",
            "text/csv",
            "title,author,description,cover_url,genres,year\nBook 1,Author 1,Desc 1,url1,Fiction,2020".getBytes()
        );
        when(bookService.importCsv(any())).thenReturn(1);

        // Act
        ResponseEntity<BookController.ImportResponse> response = bookController.importBooks(file);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().imported());
        verify(bookService).importCsv(any());
    }
}
