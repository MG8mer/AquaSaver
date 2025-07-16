package com.example.aquasaver.dao;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.aquasaver.challenges.ChallengeWithProgress;
import com.example.aquasaver.model.Challenges;
import com.example.aquasaver.model.Suggestions;

// Import list
import java.util.List;

@Dao
public interface SuggestionsDao
{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertSuggestion(Suggestions suggestion);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllSuggestions(List<Suggestions> suggestions);

    // Update queries
    @Update
    void updateSuggestion(Suggestions suggestion);

    /*

    Delete queries

     */
    @Delete
    void deleteSuggestion(Suggestions suggestion); // deletes a specific suggestion

    @Query("DELETE FROM suggestions WHERE user_email = :userEmail")
    int deleteSuggestionsByEmail(String userEmail); // deletes all user suggestions and return # rows deleted

    /*

    Select queries

     */


    @Query("SELECT * FROM suggestions WHERE user_email = :userEmail")
    List<Suggestions> getUserSuggestions(String userEmail); // Returns all suggestions of a particular user

    @Query("SELECT * FROM suggestions WHERE title = :title LIMIT 1")
    Suggestions getSuggestionByTitle(String title);

}