package com.example.aquasaver.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

// Import WaterUsage class
import com.example.aquasaver.model.WaterUsage;

import java.util.List;

@Dao
public interface WaterUsageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) //single insert
    long insertLog(WaterUsage waterUsed); // Returns row ID of the newly logged usage

    @Insert(onConflict = OnConflictStrategy.REPLACE) //multiple inserts
    void insertAllLogs(List<WaterUsage> waterUsed);

    @Update
    void updateLog(WaterUsage waterUsed); //updates log

    @Delete
    void deleteLog(WaterUsage waterUsed); //deletes log


    @Query("DELETE FROM water_usage_log WHERE user_email = :userEmail")
    int deleteLogsForUser(String userEmail); //deletes all logs for a specific user and returns number of rows deleted

    @Query("DELETE FROM water_usage_log WHERE date < :dateTimestamp")
    int deleteLogsOlderThan(long dateTimestamp); //Deletes all logs older than a specific date for a specific user and returns number of rows deleted

    @Query("SELECT * FROM water_usage_log WHERE id = :logId LIMIT 1")
    WaterUsage getLogById(int logId); // Retrieves specific log

    @Query("SELECT * FROM water_usage_log WHERE user_email = :userEmail ORDER BY date DESC")
    List<WaterUsage> getLogsForUser(String userEmail); //Returns a list of all logs, ordered by date (most recent first).

    @Query("SELECT * FROM water_usage_log WHERE user_email = :userEmail AND date = :dateTimestamp ORDER BY id DESC")
    List<WaterUsage> getLogsForUserAndDate(String userEmail, long dateTimestamp); //Returns a list of logs for a specific user and specific date.

    @Query("SELECT * FROM water_usage_log WHERE user_email = :userEmail AND date = :dateTimestamp ORDER BY id DESC LIMIT 1")
    WaterUsage getLatestLogForUserAndDate(String userEmail, long dateTimestamp); //Returns the most recent log for a specific user on a given date.

    @Query("SELECT * FROM water_usage_log WHERE user_email = :userEmail AND date >= :startDateTimestamp AND date <= :endDateTimestamp ORDER BY date DESC")
    List<WaterUsage> getLogsForUserInDateRange(String userEmail, long startDateTimestamp, long endDateTimestamp); //Returns a list of logs for a specific user within a date range.
}
