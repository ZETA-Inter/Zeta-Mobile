package com.example.feature_produtor.dto.response;

import com.example.core.model.mongo.FlashCard;

import java.util.List;

public class ClassesDetaisResponseDTO {


    private Integer id;
    private String title;
    private String description;


    private List<FlashCard> flashcards;

    private List<Law> laws;


    private List<String> content;


    public static class Law {
        private String number;
        private String description;

        // Getters
        public String getNumber() { return number; }
        public String getDescription() { return description; }
    }

    public ClassesDetaisResponseDTO() {}


    // --- GETTERS (Incluindo getFlashcards) ---
    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<FlashCard> getFlashcards() {
        return flashcards;
    }

    public List<Law> getLaws() {
        return laws;
    }

    public List<String> getContent() {
        return content;
    }
}