package com.example.aquasaver.model;

// Import important room db libraries
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "challenge_progress",
        foreignKeys = @ForeignKey(entity = UserProfile.class,
                parentColumns = "email",
                childColumns = "user_email",
                onDelete = ForeignKey.CASCADE,
                onUpdate = ForeignKey.NO_ACTION),
        indices = {@Index(value = {"user_email"})})
public class ChallengeProgress {

    @PrimaryKey(autoGenerate = true)
    public int id;

    // Instance variables for challenge progress db entity columns
    @NonNull
    @ColumnInfo(name = "user_email")
    public String userEmail;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "current_progress")
    public float currentProgress;

    @ColumnInfo(name = "completion")
    public boolean completion;

    @ColumnInfo(name = "timestamp")
    public Date timestamp;

    // Constructor
    public ChallengeProgress(@NonNull String userEmail, String title, float currentProgress, boolean completion, Date timestamp) {
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
