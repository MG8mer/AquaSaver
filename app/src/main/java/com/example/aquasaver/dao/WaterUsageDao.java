package com.example.aquasaver.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

// Import WaterUsage class
import com.example.aquasaver.model.WaterUsage;
import com.example.aquasaver.ui.water_usage.DailyUsage;

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

    @Query("SELECT COALESCE(SUM(amount_liters), 0)  " +
            "FROM water_usage_log " +
            "WHERE date(date / 1000, 'unixepoch') = date('now') " +
            "AND user_email = :email")
    Float getLitersUsedToday(String email);

    @Query("SELECT COALESCE(SUM(amount_liters), 0) " +
            "FROM water_usage_log " +
            "WHERE date(date / 1000, 'unixepoch') >= date('now', '-6 days') " +
            "AND date(date / 1000, 'unixepoch') <= date('now') " +
            "AND user_email = :email")
    Float getLitersUsedThisWeek(String email);

    @Query("SELECT SUM(amount_liters) FROM water_usage_log WHERE user_email = :email AND date BETWEEN :start AND :end")
    float getLitersUsedBetween(String email, long start, long end);

    @Query("SELECT * FROM water_usage_log WHERE user_email = :userEmail ORDER BY date DESC LIMIT 1")
    WaterUsage getLatestLogForUser(String userEmail);

    @Query("SELECT date(date / 1000, 'unixepoch') as day, SUM(amount_liters) as total_liters " +
            "FROM water_usage_log " +
            "WHERE user_email = :email AND date BETWEEN :start AND :end " +
            "GROUP BY day " +
            "ORDER BY day ASC")
    List<DailyUsage> getDailyUsageBetween(String email, long start, long end);

    @Query("SELECT date(date / 1000, 'unixepoch') as day, SUM(amount_liters) as total_liters " +
            "FROM water_usage_log " +
            "WHERE user_email = :email AND date(date / 1000, 'unixepoch') = date(:dayTimestamp / 1000, 'unixepoch') " +
            "GROUP BY day " +
            "LIMIT 1")
    DailyUsage getDailyUsageForDay(String email, long dayTimestamp);

    @Query("SELECT date(date / 1000, 'unixepoch', 'localtime') as day, SUM(amount_liters) as total_liters " +
            "FROM water_usage_log " +
            "WHERE user_email = :email " +
            "GROUP BY day " +
            "ORDER BY day ASC")
    List<DailyUsage> getAllDailyUsageForUser(String email);

}

