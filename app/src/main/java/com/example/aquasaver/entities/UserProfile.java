package com.example.aquasaver.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import com.example.aquasaver.entities.enums.GoalType;

import java.util.Date;


@Entity(tableName = "user_profiles",
        primaryKeys = {"email"}) // Define composite primary key here
public class UserProfile {
    @NonNull
    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "location")
    public String location;

    @ColumnInfo(name = "goal_type")
    public GoalType goalType; // Enum for goal types

    @ColumnInfo(name = "password")
    public String passwordHash; // Store a hash of the password

    @ColumnInfo(name = "use_gps")
    public boolean useGPS;

    @ColumnInfo(name = "notifications_on")
    public boolean notificationsOn;

    @ColumnInfo(name = "reminder_time")
    public String reminderTime;

    @ColumnInfo(name = "weather_alerts_enabled", defaultValue = "false")
    public boolean weatherAlertsEnabled;

    @ColumnInfo(name = "join_date")
    public Date joinDate;

    public UserProfile(@NonNull String email, String passwordHash, String location, boolean useGPS,
                       GoalType goalType, boolean notificationsOn, String reminderTime, boolean weatherAlertsEnabled, Date joinDate) { // Constructor
        this.email = email;
        this.location = location;
        this.passwordHash = passwordHash;
        this.useGPS = useGPS;
        this.goalType = goalType;
        this.notificationsOn = notificationsOn;
        this.reminderTime = reminderTime;
        this.weatherAlertsEnabled = weatherAlertsEnabled;
        this.joinDate = joinDate;
    }

    public String getEmail() { // Getter for email
        return email;
    }

    public void setEmail(String email) { // Setter for email
        this.email = email;
    }

    public String getPasswordHash() { // Getter for password hash
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) { // Setter for password hash
        this.passwordHash = passwordHash;
    }

    public String getLocation() { // Getter for location
        return location;
    }

    public void setLocation(String location) { // Setter for location
        this.location = location;
    }

    public boolean isUseGPS() { // Getter for useGPS
        return useGPS;
    }

    public void setUseGPS(boolean useGPS) { // Setter for useGPS
        this.useGPS = useGPS;
    }

    public GoalType getGoalType() { // Getter for goal type
        return goalType;
    }

    public void setGoalType(GoalType goalType) { // Setter for goal type
        this.goalType = goalType;
    }

    public boolean isNotificationsOn() { // Getter for notificationsOn
        return notificationsOn;
    }

    public void setNotificationsOn(boolean notificationsOn) { // Setter for notificationsOn
        this.notificationsOn = notificationsOn;
    }

    public String getReminderTime() { return reminderTime;}

    public void setReminderTime(String reminderTime) {this.reminderTime = reminderTime; }

    public boolean isWeatherAlertsEnabled() {return weatherAlertsEnabled; }

    public void setWeatherAlertsEnabled(boolean weatherAlertsEnabled) {this.weatherAlertsEnabled = weatherAlertsEnabled; }

    public Date getJoinDate() { // Getter for joinDate
        return joinDate;
    }

    public void setJoinDate(Date joinDate) { // Setter for joinDate
        this.joinDate = joinDate;
    }

}