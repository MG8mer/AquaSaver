package com.example.aquasaver.model;

public class Suggestions {
    private int id;
    private String userEmail;
    private String condition;
    private String title;
    private String description;

    public Suggestions() {}

    public Suggestions(String userEmail, String condition, String title, String description) {
        this.userEmail = userEmail;
        this.condition = condition;
        this.title = title;
        this.description = description;
    }

    // Getters and setters
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
