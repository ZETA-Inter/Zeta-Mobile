package com.example.core.model.mongo;

import java.util.List;

public class Class {


    private Integer id;

    private String title;

    private List<String> content;

    private String description;

    private List<FlashCard> flashcards;


    private List<Law> laws;


    public Class() {
    }

    // --- Getters ---

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getContent() {
        return content;
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

    // --- Setters ---

    public void setId(Integer id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(List<String> content) {
        this.content = content;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public void setFlashcards(List<FlashCard> flashcards) {
        this.flashcards = flashcards;
    }

    public void setLaws(List<Law> laws) {
        this.laws = laws;
    }

    @Override
    public String toString() {
        return "Class{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content=" + content +
                ", description='" + description + '\'' +
                ", flashcards=" + flashcards +
                ", laws=" + laws +
                '}';
    }
}