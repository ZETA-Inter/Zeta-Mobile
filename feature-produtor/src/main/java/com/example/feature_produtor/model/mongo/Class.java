package com.example.feature_produtor.model.mongo;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Class {


    private Integer id;

    @SerializedName("program_id")
    private Integer programId;

    private String title;

    private List<Content> content;

    private String description;

    private List<FlashCard> flashcards;


    private List<Law> laws;


    public Class() {
    }

    // --- Getters ---

    public Integer getId() {
        return id;
    }

    public Integer getProgramId() {
        return programId;
    }

    public String getTitle() {
        return title;
    }

    public List<Content> getContent() {
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

    public void setProgramId(Integer programId) {
        this.programId = programId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(List<Content> content) {
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
}