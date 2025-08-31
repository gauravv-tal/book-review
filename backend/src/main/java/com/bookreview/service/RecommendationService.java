package com.bookreview.service;

import com.bookreview.dto.AiRecommendationDto;
import com.bookreview.model.Book;
import com.bookreview.model.Favourite;
import com.bookreview.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.jpa.domain.Specification;
import com.bookreview.dto.AiRecommendationDto;

@Service
public class RecommendationService {
    private final BookRepository bookRepo;
    private final FavouriteService favService;
    private final GeminiAIService geminiAIService;
    private final ObjectMapper mapper = new ObjectMapper();

    public RecommendationService(BookRepository bookRepository, FavouriteService favouriteService, GeminiAIService geminiAIService) {
        this.bookRepo = bookRepository;
        this.favService = favouriteService;
        this.geminiAIService = geminiAIService;
    }

    public List<Book> getTopRated(int limit) {
        return bookRepo.findTopRated(PageRequest.of(0, Math.max(1, limit)));
    }

    public List<AiRecommendationDto> getAiRecommendations(Long userId, int limit) {
        // Build prompt from user's favourites: title, author, genres
        List<Favourite> favs = favService.findByUserId(userId);
        if (favs.isEmpty()) {
            return Collections.emptyList();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("You are a book recommendation assistant.\n");
        sb.append("Given the following user's favourite books (with title, author, and genres), recommend ")
          .append(Math.max(1, limit)).append(" books that the user is likely to enjoy.\n");
        sb.append("Return STRICT JSON ONLY: an array of objects with fields: title, author, reason. No markdown, no explanation.\n\n");
        sb.append("User favourites:\n");
        int n = 1;
        for (Favourite f : favs) {
            Book b = f.getBook();
            sb.append(n++).append(". Title: ").append(Optional.ofNullable(b.getTitle()).orElse(""))
              .append(", Author: ").append(Optional.ofNullable(b.getAuthor()).orElse(""))
              .append(", Genres: ").append(Optional.ofNullable(b.getGenres()).orElse(""))
              .append("\n");
        }
        String prompt = sb.toString();

        try {
            return callGeminiForJson(prompt, limit);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private List<AiRecommendationDto> callGeminiForJson(String prompt, int limit) throws Exception {
        String payload = geminiAIService.generateContent(prompt);
        String content = geminiAIService.extractCleanJsonFromResponse(payload);
        if (content.isBlank()) return Collections.emptyList();

        // Parse the JSON array of books from content
        List<Map<String, String>> recs = mapper.readValue(content, List.class);
        return recs.stream()
                .limit(limit)
                .map(r -> new AiRecommendationDto(
                        r.get("title"),
                        r.get("author")
                ))
                .collect(Collectors.toList());
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
}
