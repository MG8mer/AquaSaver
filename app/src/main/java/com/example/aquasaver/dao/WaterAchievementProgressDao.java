package com.example.aquasaver.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface WaterAchievementProgressDao {
    /*
    @Insert(onConflict = OnConflictStrategy.REPLACE) //single insert

    @Insert(onConflict = OnConflictStrategy.REPLACE) //multiple inserts
    void insertAllProgressions(List<WaterAchievementProgress> progression);

    @Update
    void updateProgression(WaterAchievementProgress progression); //updates progression

    @Delete
    void deleteProgression(WaterAchievementProgress progression); //deletes progression


    @Query("DELETE FROM water_achievement_progress WHERE user_email = :userEmail")
    int deleteProgressionForUser(String userEmail); //deletes all progression for a specific user and returns number of rows deleted

    @Query("DELETE FROM water_achievement_progress WHERE date < :dateTimestamp")
    int deleteProgressionsOlderThan(long dateTimestamp); //Deletes all progression older than a specific date for a specific user and returns number of rows deleted

    @Query("SELECT * FROM water_achievement_progress WHERE id = :progressId LIMIT 1")
    WaterAchievementProgress getProgressionById(int progressId); // Retrieves specific progress

    @Query("SELECT * FROM water_achievement_progress WHERE user_email = :userEmail ORDER BY date DESC")
    List<WaterAchievementProgress> getLogsForUser(String userEmail); //Returns a list of all logs, ordered by date (most recent first).

    @Query("SELECT * FROM water_achievement_progress WHERE user_email = :userEmail AND date = :dateTimestamp ORDER BY id DESC")
    List<WaterAchievementProgress> getLogsForUserAndDate(String userEmail, long dateTimestamp); //Returns a list of logs for a specific user and specific date.

    @Query("SELECT * FROM water_achievement_progress WHERE user_email = :userEmail AND date = :dateTimestamp ORDER BY id DESC LIMIT 1")
    WaterAchievementProgress getLatestLogForUserAndDate(String userEmail, long dateTimestamp); //Returns the most recent log for a specific user on a given date.

    @Query("SELECT * FROM water_achievement_progress WHERE user_email = :userEmail AND date >= :startDateTimestamp AND date <= :endDateTimestamp ORDER BY date DESC")
    List<WaterAchievementProgress> getLogsForUserInDateRange(String userEmail, long startDateTimestamp, long endDateTimestamp); //Returns a list of logs for a specific user within a date range.
*/
}
