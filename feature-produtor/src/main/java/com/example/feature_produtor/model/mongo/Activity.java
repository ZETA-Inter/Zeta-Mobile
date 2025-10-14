package com.example.feature_produtor.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Activity {


    private Integer id;

    @SerializedName("class_id")
    private Integer classId;

    private Integer points;

    private List<Question> questions;

    public Activity() {}

    // Getters e Setters


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getClassId() {
        return classId;
    }

    public void setClassId(Integer classId) {
        this.classId = classId;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }


    // Classe Interna (Question)
    public static class Question {

        @SerializedName("image_url")
        private String imageUrl;

        private String question;

        private List<Answer> answers;

        public Question() {}
        // Getters e Setters


        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public List<Answer> getAnswers() {
            return answers;
        }

        public void setAnswers(List<Answer> answers) {
            this.answers = answers;
        }


        // Classe Interna (Answer)
        public static class Answer {

            private String answer;

            private boolean correct;

            public Answer() {}
            // Getters e Setters
            public String getAnswer() { return answer; }
            public void setAnswer(String answer) { this.answer = answer; }
            public boolean isCorrect() { return correct; }
            public void setCorrect(boolean correct) { this.correct = correct; }
        }
    }
}