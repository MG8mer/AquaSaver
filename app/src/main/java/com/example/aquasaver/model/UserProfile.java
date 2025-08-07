package com.example.aquasaver.model;

import com.example.aquasaver.model.enums.GoalType;
import java.util.Date;

public class UserProfile {
    private String email;
    private String location;
    private GoalType goalType;
    private String passwordHash;
    private boolean useGPS;
    private boolean notificationsOn;
    private String reminderTime;
    private boolean weatherAlertsEnabled;
    private Date joinDate;
    private int streak;
    private Date lastStreakUpdate;
    private String salt;

    public UserProfile() {}

    public UserProfile(String email, String passwordHash, String salt, String location, boolean useGPS,
                       GoalType goalType, boolean notificationsOn, String reminderTime,
                       boolean weatherAlertsEnabled, Date joinDate, Date lastStreakUpdate) {
        this.email = email;
        this.location = location;
        this.salt = salt;
        this.passwordHash = passwordHash;
        this.useGPS = useGPS;
        this.goalType = goalType;
        this.notificationsOn = notificationsOn;
        this.reminderTime = reminderTime;
        this.weatherAlertsEnabled = weatherAlertsEnabled;
        this.joinDate = joinDate;
        this.lastStreakUpdate = lastStreakUpdate;
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

    public String getSalt()
    {
        return salt;
    }

    public void setSalt(String salt)
    {
        this.salt = salt;
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
    public int getStreak() { return streak; }
    public void setStreak(int streak) { this.streak = streak; }

    public Date getLastStreakUpdate() { return lastStreakUpdate; }
    public void setLastStreakUpdate(Date lastStreakUpdate) { this.lastStreakUpdate = lastStreakUpdate; }
}