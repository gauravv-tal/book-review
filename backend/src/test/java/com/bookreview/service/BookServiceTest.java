package com.bookreview.service;

import com.bookreview.model.Book;
import com.bookreview.repository.BookRepository;
import org.apache.commons.csv.CSVFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookServiceTest {
    private BookRepository bookRepository;
    private BookService bookService;

    @BeforeEach
    void setup() {
        bookRepository = Mockito.mock(BookRepository.class);
        bookService = new BookService(bookRepository);
    }

    @Test
    void findById_delegatesToRepo() {
        Book b = new Book(); b.setId(1L);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(b));
        Optional<Book> res = bookService.findById(1L);
        assertTrue(res.isPresent());
        assertEquals(1L, res.get().getId());
        verify(bookRepository).findById(1L);
    }
    
    @Test
    void findById_notFound_returnsEmpty() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());
        Optional<Book> res = bookService.findById(999L);
        assertTrue(res.isEmpty());
        verify(bookRepository).findById(999L);
    }

    @Test
    void search_buildsSpec_callsRepo() {
        Page<Book> page = new PageImpl<>(List.of(new Book()));
        when(bookRepository.findAll(Mockito.<org.springframework.data.jpa.domain.Specification<com.bookreview.model.Book>>any(), any(Pageable.class))).thenReturn(page);
        
        // Test with all parameters
        Page<Book> res = bookService.search("Dune", "Herbert", "Sci-Fi", 1965, PageRequest.of(0, 10));
        assertEquals(1, res.getTotalElements());
        
        // Test with null/empty parameters
        res = bookService.search(null, "", null, null, PageRequest.of(0, 10));
        assertEquals(1, res.getTotalElements());
        
        verify(bookRepository, times(2)).findAll(Mockito.<org.springframework.data.jpa.domain.Specification<com.bookreview.model.Book>>any(), any(Pageable.class));
    }

    @Test
    void importCsv_parsesAndSavesValidRows_only() throws Exception {
        String csv = "title,author,description,cover_url,genres,year\n" +
                     "Dune,Frank Herbert,desc,,Sci-Fi,1965\n" +
                     ",No Author,desc,,Genre,2000\n" +
                     "Foundation,Isaac Asimov,desc,,Sci-Fi,notayear\n";
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));

        int imported = bookService.importCsv(file);
        assertEquals(2, imported); // second row skipped (missing title), third saved with null year
        verify(bookRepository, times(2)).save(any(Book.class));
    }
    
    @Test
    void importCsv_emptyFile_returnsZero() throws Exception {
        String csv = "title,author,description,cover_url,genres,year\n";
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));
        
        int imported = bookService.importCsv(file);
        assertEquals(0, imported);
        verify(bookRepository, never()).save(any(Book.class));
    }
    
    @Test
    void importCsv_invalidCsv_throwsException() throws Exception {
        String invalidCsv = "id,name,age\n"
                + "1,John Doe,30\n"
                + "2,Jane Smith,\"twenty-five\n"
                + "3,Bob,40,extra-field\n"
                + ",MissingId,22\n";
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(invalidCsv.getBytes(StandardCharsets.UTF_8)));
        
        assertThrows(RuntimeException.class, () -> bookService.importCsv(file));
        verify(bookRepository, never()).save(any(Book.class));
    }
}
