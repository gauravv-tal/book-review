package com.bookreview.service;

import com.bookreview.model.Book;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class BookSearchPredicateTest {

    @Test
    void buildSearchPredicate_withTitleOnly() {
        // Given
        String title = "Dune";
        
        // Setup mocks
        Root<Book> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        
        // Mock the expression chain: root.get("title") -> titleExpr
        Path titlePath = mock(Path.class);
        when(root.get("title")).thenReturn(titlePath);
        
        // Mock the lower() call
        Expression<String> lowerTitle = mock(Expression.class);
        when(cb.lower(titlePath)).thenReturn(lowerTitle);
        
        // Mock the like() call
        Predicate titlePredicate = mock(Predicate.class);
        when(cb.like(lowerTitle, "%dune%")).thenReturn(titlePredicate);

        // When
        List<Predicate> predicates = BookService.buildSearchPredicate(title, null, null, null, root, cb);

        // Then
        assertEquals(1, predicates.size());
        assertSame(titlePredicate, predicates.get(0));
        
        // Verify the interactions
        verify(root).get("title");
        verify(cb).lower(titlePath);
        verify(cb).like(lowerTitle, "%dune%");
        verifyNoMoreInteractions(root, cb);
    }

    @Test
    void buildSearchPredicate_withAuthorOnly() {
        // Given
        String author = "Herbert";
        
        // Setup mocks
        Root<Book> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        
        // Mock the expression chain: root.get("author") -> authorPath
        Path authorPath = mock(Path.class);
        when(root.get("author")).thenReturn(authorPath);
        
        // Mock the lower() call
        Expression<String> lowerAuthor = mock(Expression.class);
        when(cb.lower(authorPath)).thenReturn(lowerAuthor);
        
        // Mock the like() call
        Predicate authorPredicate = mock(Predicate.class);
        when(cb.like(lowerAuthor, "%herbert%")).thenReturn(authorPredicate);

        // When
        List<Predicate> predicates = BookService.buildSearchPredicate(null, author, null, null, root, cb);

        // Then
        assertEquals(1, predicates.size());
        assertSame(authorPredicate, predicates.get(0));
        
        // Verify the interactions
        verify(root).get("author");
        verify(cb).lower(authorPath);
        verify(cb).like(lowerAuthor, "%herbert%");
        verifyNoMoreInteractions(root, cb);
    }

    @Test
    void buildSearchPredicate_withGenreOnly() {
        // Given
        String genre = "Sci-Fi";
        
        // Setup mocks
        Root<Book> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        
        // Mock the expression chain: root.get("genres") -> genresPath
        Path genresPath = mock(Path.class);
        when(root.get("genres")).thenReturn(genresPath);
        
        // Mock the lower() call
        Expression<String> lowerGenres = mock(Expression.class);
        when(cb.lower(genresPath)).thenReturn(lowerGenres);
        
        // Mock the like() call
        Predicate genrePredicate = mock(Predicate.class);
        when(cb.like(lowerGenres, "%sci-fi%")).thenReturn(genrePredicate);

        // When
        List<Predicate> predicates = BookService.buildSearchPredicate(null, null, genre, null, root, cb);

        // Then
        assertEquals(1, predicates.size());
        assertSame(genrePredicate, predicates.get(0));
        
        // Verify the interactions
        verify(root).get("genres");
        verify(cb).lower(genresPath);
        verify(cb).like(lowerGenres, "%sci-fi%");
        verifyNoMoreInteractions(root, cb);
    }

    @Test
    void buildSearchPredicate_withYearOnly() {
        // Given
        int year = 1965;
        
        // Setup mocks
        Root<Book> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        
        // Mock the expression: root.get("year") -> yearPath
        Path yearPath = mock(Path.class);
        when(root.get("year")).thenReturn(yearPath);
        
        // Mock the equal() call
        Predicate yearPredicate = mock(Predicate.class);
        when(cb.equal(yearPath, year)).thenReturn(yearPredicate);

        // When
        List<Predicate> predicates = BookService.buildSearchPredicate(null, null, null, year, root, cb);

        // Then
        assertEquals(1, predicates.size());
        assertSame(yearPredicate, predicates.get(0));
        
        // Verify the interactions
        verify(root).get("year");
        verify(cb).equal(yearPath, year);
        verifyNoMoreInteractions(root, cb);
    }

    @Test
    void buildSearchPredicate_withAllParameters() {
        // Given
        String title = "Dune";
        String author = "Herbert";
        String genre = "Sci-Fi";
        int year = 1965;
        
        // Setup mocks
        Root<Book> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        
        // Title mocks
        Path titlePath = mock(Path.class);
        Expression<String> lowerTitle = mock(Expression.class);
        Predicate titlePredicate = mock(Predicate.class);
        
        // Author mocks
        Path authorPath = mock(Path.class);
        Expression<String> lowerAuthor = mock(Expression.class);
        Predicate authorPredicate = mock(Predicate.class);
        
        // Genre mocks
        Path genresPath = mock(Path.class);
        Expression<String> lowerGenres = mock(Expression.class);
        Predicate genrePredicate = mock(Predicate.class);
        
        // Year mocks
        Path yearPath = mock(Path.class);
        Predicate yearPredicate = mock(Predicate.class);
        
        // Setup root.get()
        when(root.get("title")).thenReturn(titlePath);
        when(root.get("author")).thenReturn(authorPath);
        when(root.get("genres")).thenReturn(genresPath);
        when(root.get("year")).thenReturn(yearPath);
        
        // Setup lower()
        when(cb.lower(titlePath)).thenReturn(lowerTitle);
        when(cb.lower(authorPath)).thenReturn(lowerAuthor);
        when(cb.lower(genresPath)).thenReturn(lowerGenres);
        
        // Setup like()
        when(cb.like(lowerTitle, "%dune%")).thenReturn(titlePredicate);
        when(cb.like(lowerAuthor, "%herbert%")).thenReturn(authorPredicate);
        when(cb.like(lowerGenres, "%sci-fi%")).thenReturn(genrePredicate);
        
        // Setup equal()
        when(cb.equal(yearPath, year)).thenReturn(yearPredicate);

        // When
        List<Predicate> predicates = BookService.buildSearchPredicate(title, author, genre, year, root, cb);

        // Then
        assertEquals(4, predicates.size());
        assertTrue(predicates.contains(titlePredicate));
        assertTrue(predicates.contains(authorPredicate));
        assertTrue(predicates.contains(genrePredicate));
        assertTrue(predicates.contains(yearPredicate));
        
        // Verify all interactions
        verify(root).get("title");
        verify(root).get("author");
        verify(root).get("genres");
        verify(root).get("year");
        
        verify(cb).lower(titlePath);
        verify(cb).lower(authorPath);
        verify(cb).lower(genresPath);
        
        verify(cb).like(lowerTitle, "%dune%");
        verify(cb).like(lowerAuthor, "%herbert%");
        verify(cb).like(lowerGenres, "%sci-fi%");
        verify(cb).equal(yearPath, year);
        
        verifyNoMoreInteractions(root, cb);
    }

    @Test
    void buildSearchPredicate_withEmptyStrings() {
        // Given
        Root<Book> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        
        // When
        List<Predicate> predicates = BookService.buildSearchPredicate("", "", "", null, root, cb);

        // Then
        assertTrue(predicates.isEmpty());
        
        // Verify no interactions with criteria builder for empty strings
        verifyNoInteractions(cb);
    }

    @Test
    void buildSearchPredicate_withNullParameters() {
        // Given
        Root<Book> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        
        // When
        List<Predicate> predicates = BookService.buildSearchPredicate(null, null, null, null, root, cb);

        // Then
        assertTrue(predicates.isEmpty());
        
        // Verify no interactions with criteria builder for null parameters
        verifyNoInteractions(cb);
    }

    @Test
    void buildSearchPredicate_withBlankStrings() {
        // Given
        Root<Book> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        
        // When
        List<Predicate> predicates = BookService.buildSearchPredicate("  ", "\t\n", " ", null, root, cb);

        // Then
        assertTrue(predicates.isEmpty());
        
        // Verify no interactions with criteria builder for blank strings
        verifyNoInteractions(cb);
    }
}
