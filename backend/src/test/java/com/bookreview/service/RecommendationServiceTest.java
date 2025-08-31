package com.bookreview.service;

import com.bookreview.dto.AiRecommendationDto;
import com.bookreview.model.Book;
import com.bookreview.model.Favourite;
import com.bookreview.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RecommendationServiceTest {
    @Mock
    private BookRepository bookRepository;
    
    @Mock
    private FavouriteService favouriteService;
    
    @Mock
    private GeminiAIService geminiAIService;
    
    private RecommendationService service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new RecommendationService(bookRepository, favouriteService, geminiAIService);
    }


    @Test
    void getTopRated_delegatesToRepo() {
        // Given
        List<Book> mockBooks = List.of(new Book(), new Book());
        when(bookRepository.findTopRated(any())).thenReturn(mockBooks);
        
        // When
        List<Book> result = service.getTopRated(5);
        
        // Then
        assertEquals(2, result.size());
        verify(bookRepository).findTopRated(PageRequest.of(0, 5));
    }

    @Test
    void getAiRecommendations_noFavourites_returnsEmpty() {
        // Given
        when(favouriteService.findByUserId(1L)).thenReturn(Collections.emptyList());
        
        // When
        List<AiRecommendationDto> result = service.getAiRecommendations(1L, 5);
        
        // Then
        assertTrue(result.isEmpty());
        verifyNoInteractions(geminiAIService);
    }
    
    @Test
    void getAiRecommendations_validRequest_returnsRecommendations() throws Exception {
        // Given
        Book favBook = new Book();
        favBook.setTitle("Dune");
        favBook.setAuthor("Frank Herbert");
        favBook.setGenres("Science Fiction");
        
        Favourite fav = Favourite.builder().book(favBook).build();
        when(favouriteService.findByUserId(1L)).thenReturn(List.of(fav));
        
        String geminiResponse = "[{\"title\":\"Foundation\",\"author\":\"Isaac Asimov\",\"reason\":\"Classic sci-fi\"}]";
        when(geminiAIService.generateContent(anyString())).thenReturn("dummy-response");
        when(geminiAIService.extractCleanJsonFromResponse(anyString())).thenReturn(geminiResponse);
        
        // When
        List<AiRecommendationDto> result = service.getAiRecommendations(1L, 5);
        
        // Then
        assertFalse(result.isEmpty());
        assertEquals("Foundation", result.get(0).getTitle());
        assertEquals("Isaac Asimov", result.get(0).getAuthor());

        verify(geminiAIService).generateContent(anyString());
        verify(geminiAIService).extractCleanJsonFromResponse(anyString());
    }
    
    @Test
    void getAiRecommendations_emptyResponse_returnsEmptyList() throws Exception {
        // Given
        Book favBook = new Book();
        favBook.setTitle("Dune");
        favBook.setAuthor("Frank Herbert");
        
        Favourite fav = Favourite.builder().book(favBook).build();
        when(favouriteService.findByUserId(1L)).thenReturn(List.of(fav));
        
        when(geminiAIService.generateContent(anyString())).thenReturn("");
        when(geminiAIService.extractCleanJsonFromResponse(anyString())).thenReturn("");
        
        // When
        List<AiRecommendationDto> result = service.getAiRecommendations(1L, 5);
        
        // Then
        assertTrue(result.isEmpty());
    }
    
    @Test
    void getAiRecommendations_invalidJson_returnsEmptyList() throws Exception {
        // Given
        Book favBook = new Book();
        favBook.setTitle("Dune");
        favBook.setAuthor("Frank Herbert");
        
        Favourite fav = Favourite.builder().book(favBook).build();
        when(favouriteService.findByUserId(1L)).thenReturn(List.of(fav));
        
        when(geminiAIService.generateContent(anyString())).thenReturn("dummy-response");
        when(geminiAIService.extractCleanJsonFromResponse(anyString())).thenReturn("{invalid-json");
        
        // When
        List<AiRecommendationDto> result = service.getAiRecommendations(1L, 5);
        
        // Then
        assertTrue(result.isEmpty());
    }
}
