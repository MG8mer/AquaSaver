package com.example.aquasaver.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import java.util.Date;

@Entity(tableName = "weather_suggestions",
        foreignKeys = @ForeignKey(entity = UserProfile.class,
                parentColumns = "email",
                childColumns = "user_email",
                onDelete = ForeignKey.CASCADE,
                onUpdate = ForeignKey.NO_ACTION))
public class WeatherSuggestions {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    @ColumnInfo(name = "user_email", index = true)
    public String userEmail;

    @ColumnInfo(name = "location")
    public String location;

    @ColumnInfo(name = "date")
    public Date date;

    @ColumnInfo(name = "usage_suggestion_text")
    public String usageSuggestionText; // The actual suggestion string from the LLM

    @ColumnInfo(name = "raw_weather_data_json")
    public String rawWeatherDataJson; // The raw JSON data from the LLM

    // Constructor
    public WeatherSuggestions(@NonNull String userEmail, String location, Date date, String usageSuggestionText) {
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

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public String getUsageSuggestionText() { return usageSuggestionText; }
    public void setUsageSuggestionText(String usageSuggestionText) { this.usageSuggestionText = usageSuggestionText; }
}