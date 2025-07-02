package com.example.aquasaver.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "notification_settings",
        foreignKeys = @ForeignKey(entity = UserProfile.class,
                parentColumns = "email",
                childColumns = "user_email",
                onDelete = ForeignKey.CASCADE))
public class NotificationSettings {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "user_email")
    public String userEmail;

    @ColumnInfo(name = "notifications_enabled", defaultValue = "true")
    public boolean notificationsEnabled;

    @ColumnInfo(name = "reminder_time")
    public String reminderTime;

    @ColumnInfo(name = "weather_alerts_enabled", defaultValue = "false")
    public boolean weatherAlertsEnabled;

    // Constructor
    // If user_email is PK:
    public NotificationSettings(@NonNull String userEmail, boolean notificationsEnabled,
                                String reminderTime, boolean weatherAlertsEnabled) {
        this.userEmail = userEmail;
        this.notificationsEnabled = notificationsEnabled;
        this.reminderTime = reminderTime;
        this.weatherAlertsEnabled = weatherAlertsEnabled;
    }

    @NonNull
    public String getUserEmail() { return userEmail; }

    public void setUserEmail(@NonNull String userEmail) { this.userEmail = userEmail;}

    public boolean isNotificationsEnabled() { return notificationsEnabled;}

    public void setNotificationsEnabled(boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }

    public String getReminderTime() { return reminderTime;}

    public void setReminderTime(String reminderTime) {this.reminderTime = reminderTime; }

    public boolean isWeatherAlertsEnabled() {return weatherAlertsEnabled; }

    public void setWeatherAlertsEnabled(boolean weatherAlertsEnabled) {this.weatherAlertsEnabled = weatherAlertsEnabled; }
}

