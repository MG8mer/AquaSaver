package com.example.aquasaver.model;

import java.util.Date;

public class ChallengeProgress {
    private int id;
    private String userEmail;
    private String title;
    private float currentProgress;
    private boolean completion;
    private Date timestamp;

    public ChallengeProgress() {}

    public ChallengeProgress(String userEmail, String title, float currentProgress, boolean completion, Date timestamp) {
        this.userEmail = userEmail;
        this.title = title;
        this.currentProgress = currentProgress;
        this.completion = completion;
        this.timestamp = timestamp;
    }

    // Getters and setters for instance variables
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public float getCurrentProgress() { return currentProgress; }
    public void setCurrentProgress(float currentProgress) { this.currentProgress = currentProgress; }

    public boolean isCompletion() { return completion; }
    public void setCompletion(boolean completion) { this.completion = completion; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
