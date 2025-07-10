package com.example.aquasaver.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

// Import WeatherSuggestions class
import com.example.aquasaver.model.WeatherSuggestions;

import java.util.Date;
import java.util.List;

@Dao
public interface WeatherSuggestionsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE) //single insert
    long insertSuggestion(WeatherSuggestions weatherSuggestion); // Returns row ID of the newly inserted suggestion

    @Insert(onConflict = OnConflictStrategy.REPLACE) //multiple inserts
    void insertAllSuggestions(List<WeatherSuggestions> suggestions);

    @Update
    void updateSuggestion(WeatherSuggestions weatherSuggestion); //updates suggestion

    @Delete
    void deleteSuggestion(WeatherSuggestions weatherSuggestion); //deletes suggestion

    @Query("DELETE FROM weather_suggestions WHERE user_email = :userEmail")
    int deleteSuggestionsForUser(String userEmail); //deletes all weather suggestions for a specific user and returns number of rows deleted

    @Query("DELETE FROM weather_suggestions WHERE date < :dateTimestamp")
    int deleteSuggestionsOlderThan(long dateTimestamp); //Deletes all weather suggestions for a specific user and returns number of rows deleted

    @Query("SELECT * FROM weather_suggestions WHERE id = :suggestionId LIMIT 1")
    WeatherSuggestions getSuggestionById(int suggestionId); // Retrieves specific weather suggestion

    @Query("SELECT * FROM weather_suggestions WHERE user_email = :userEmail ORDER BY date DESC")
    List<WeatherSuggestions> getSuggestionsForUser(String userEmail); //Returns a list of all weather suggestions, ordered by date (most recent first).

    @Query("SELECT * FROM weather_suggestions WHERE user_email = :userEmail AND date = :dateTimestamp ORDER BY id DESC")
    List<WeatherSuggestions> getSuggestionsForUserAndDate(String userEmail, long dateTimestamp); //Returns a list of WeatherSuggestions for a specific user and specific date.

    @Query("SELECT * FROM weather_suggestions WHERE user_email = :userEmail AND date = :dateTimestamp ORDER BY id DESC LIMIT 1")
    WeatherSuggestions getLatestSuggestionForUserAndDate(String userEmail, long dateTimestamp); //Returns the most recent weather suggestion for a specific user on a given date.

    @Query("SELECT * FROM weather_suggestions WHERE user_email = :userEmail AND date >= :startDateTimestamp AND date <= :endDateTimestamp ORDER BY date DESC")
    List<WeatherSuggestions> getSuggestionsForUserInDateRange(String userEmail, long startDateTimestamp, long endDateTimestamp); //Returns a list of suggestions for a specific user within a date range.

    @Query("SELECT * FROM weather_suggestions WHERE user_email = :email AND location = :loc AND date = :date LIMIT 1")
    WeatherSuggestions getSuggestionForUserAndDate(String email, String loc, Date date);

}