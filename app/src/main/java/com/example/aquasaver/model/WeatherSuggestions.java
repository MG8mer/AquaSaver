package com.example.aquasaver.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "weather_suggestions",
        foreignKeys = {
                @ForeignKey(entity = UserProfile.class, // Assuming UserProfile is the parent for user_email
                        parentColumns = "email",    // Or {"email", "location"} if UserProfile has composite PK
                        childColumns = "user_email",
                        onDelete = ForeignKey.CASCADE) // If UserProfile deleted, delete their suggestions
        },
    indices = {
    @Index(value = {"user_email"}),
    @Index(value = {"user_email", "date"}) // For efficient lookup of suggestions for a user on a specific date
            })
public class WeatherSuggestions {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    @ColumnInfo(name = "user_email", index = true)
    public String userEmail;

    @NonNull
    @ColumnInfo(name = "location")
    public String location;

    @NonNull
    @ColumnInfo(name = "date")
    public Long date;

    @NonNull
    @ColumnInfo(name = "usage_suggestion_text")
    public String usageSuggestionText; // The actual suggestion string from the LLM

    // Constructor
    public WeatherSuggestions(@NonNull String userEmail, @NonNull String location, @NonNull Long date,
                              @NonNull String usageSuggestionText) {
        this.userEmail = userEmail;
        this.location = location;
        this.date = date;
        this.usageSuggestionText = usageSuggestionText;
    }

    public int getId() { return id; }

    @NonNull
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(@NonNull String userEmail) { this.userEmail = userEmail; }

    @NonNull
    public String getLocation() { return location; }
    public void setLocation(@NonNull String location) { this.location = location; }

    @NonNull
    public Long getDate() { return date; }
    public void setDate(@NonNull Long date) { this.date = date; }

    @NonNull
    public String getUsageSuggestionText() { return usageSuggestionText; }
    public void setUsageSuggestionText(@NonNull String usageSuggestionText) { this.usageSuggestionText = usageSuggestionText; }
}