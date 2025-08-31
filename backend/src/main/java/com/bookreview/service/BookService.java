package com.bookreview.service;

import com.bookreview.model.Book;
import com.bookreview.repository.BookRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.criteria.Predicate;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }

    public Page<Book> search(String title, String author, String genre, Integer year, Pageable pageable) {
        Specification<Book> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (title != null && !title.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase(Locale.ROOT) + "%"));
            }
            if (author != null && !author.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("author")), "%" + author.toLowerCase(Locale.ROOT) + "%"));
            }
            if (genre != null && !genre.isBlank()) {
                // simple contains in comma-separated string
                predicates.add(cb.like(cb.lower(root.get("genres")), "%" + genre.toLowerCase(Locale.ROOT) + "%"));
            }
            if (year != null) {
                predicates.add(cb.equal(root.get("year"), year));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return bookRepository.findAll(spec, pageable);
    }

    public int importCsv(MultipartFile file) throws Exception {
        int imported = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {
            for (CSVRecord record : parser) {
                Book b = new Book();
                b.setTitle(getValue(record, "title"));
                b.setAuthor(getValue(record, "author"));
                b.setDescription(getValue(record, "description"));
                b.setCoverUrl(getValue(record, "cover_url"));
                b.setGenres(getValue(record, "genres"));
                String yearStr = getValue(record, "year");
                if (!yearStr.isEmpty()) {
                    try { b.setYear(Integer.parseInt(yearStr)); } catch (NumberFormatException ignored) { b.setYear(null); }
                }
                if (b.getTitle().isEmpty() || b.getAuthor().isEmpty()) {
                    // skip invalid row
                    continue;
                }
                bookRepository.save(b);
                imported++;
            }
        }
        return imported;
    }

    private static String getValue(CSVRecord record, String header) {
        try {
            if (record.isMapped(header)) {
                String val = record.get(header);
                return val == null ? "" : val.trim();
            }
        } catch (IllegalArgumentException ignored) {
            // header not present; fall through
        }
        return "";
    }
}
