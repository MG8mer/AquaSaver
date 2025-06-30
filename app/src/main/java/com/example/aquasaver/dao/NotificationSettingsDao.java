package com.example.aquasaver.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface NotificationSettingsDao {
/*
    @Update
    int updateSettings(NotificationSettings notificationSettings);

    @Query("SELECT * FROM notification_settings WHERE user_email = :userEmail LIMIT 1")
    NotificationSettings getSettingsForUser(String userEmail);

    @Query("SELECT notifications_enabled FROM notification_settings WHERE user_email = :userEmail LIMIT 1")
    Boolean areNotificationsEnabled(String userEmail); // Returns Boolean object to handle null if no settings row

    @Query("SELECT reminder_time FROM notification_settings WHERE user_email = :userEmail LIMIT 1")
    String getReminderTimeForUser(String userEmail); // Returns null if no settings row

    @Query("SELECT weather_alerts_enabled FROM notification_settings WHERE user_email = :userEmail LIMIT 1")
    Boolean areWeatherAlertsEnabled(String userEmail); // Returns Boolean object to handle null
*/
}