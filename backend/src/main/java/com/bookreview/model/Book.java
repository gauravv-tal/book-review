package com.bookreview.model;

import jakarta.persistence.*;

@Entity
@Table(name = "books", indexes = {
        @Index(name = "idx_books_title", columnList = "title"),
        @Index(name = "idx_books_author", columnList = "author"),
        @Index(name = "idx_books_year", columnList = "year")
})
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_url")
    private String coverUrl;

    // Comma-separated genres for MVP; can be normalized later
    @Column(length = 512)
    private String genres;

    @Column(name = "year")
    private Integer year;

    // Rating aggregates
    @Column(name = "avg_rating")
    private Double avgRating;

    @Column(name = "review_count", nullable = true)
    private Long reviewCount = 0L;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public String getGenres() { return genres; }
    public void setGenres(String genres) { this.genres = genres; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Double getAvgRating() { return avgRating; }
    public void setAvgRating(Double avgRating) { this.avgRating = avgRating; }

    public Long getReviewCount() { return reviewCount; }
    public void setReviewCount(Long reviewCount) { this.reviewCount = reviewCount; }
}
