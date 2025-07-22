package com.example.aquasaver.model;
import java.util.Date;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
@Entity(tableName = "suggestions",
        foreignKeys = @ForeignKey(entity = UserProfile.class,
                parentColumns = "email",
                childColumns = "user_email",
                onDelete = ForeignKey.CASCADE,
                onUpdate = ForeignKey.NO_ACTION),
        indices = {@Index(value = {"user_email"})})
public class Suggestions {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    @ColumnInfo(name = "user_email")
    public String userEmail;

    @ColumnInfo(name = "condition")
    public String condition;  // New field

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "description")
    public String description;

    // Constructor with condition
    public Suggestions(@NonNull String userEmail,
                       String condition,
                       String title,
                       String description) {
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
