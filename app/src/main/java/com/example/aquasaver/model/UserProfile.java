package com.example.aquasaver.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import com.example.aquasaver.model.enums.GoalType;


@Entity(tableName = "user_profiles",
        primaryKeys = {"email", "location", "goal_type"}) // Define composite primary key here
public class UserProfile {
    @NonNull
    @ColumnInfo(name = "email")
    public String email;

    @NonNull
    @ColumnInfo(name = "location")
    public String location;

    @NonNull
    @ColumnInfo(name = "goal_type")
    public GoalType goalType; // Enum for goal types

    @ColumnInfo(name = "password")
    public String passwordHash; // Store a hash of the password

    @ColumnInfo(name = "use_gps")
    public boolean useGPS;

    @ColumnInfo(name = "notifications_on")
    public boolean notificationsOn;

    @ColumnInfo(name = "join_date")
    public Long joinDate;

    public UserProfile(@NonNull String email, String passwordHash, @NonNull String location, boolean useGPS,
                       @NonNull GoalType goalType, boolean notificationsOn, Long joinDate) { // Constructor
        this.email = email;
        this.location = location;
        this.passwordHash = passwordHash;
        this.useGPS = useGPS;
        this.goalType = goalType;
        this.notificationsOn = notificationsOn;
        this.joinDate = joinDate;
    }

    @NonNull
    public String getEmail() { // Getter for email
        return email;
    }

    public void setEmail(@NonNull String email) { // Setter for email
        this.email = email;
    }

    public String getPasswordHash() { // Getter for password hash
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) { // Setter for password hash
        this.passwordHash = passwordHash;
    }

    @NonNull
    public String getLocation() { // Getter for location
        return location;
    }

    public void setLocation(@NonNull String location) { // Setter for location
        this.location = location;
    }

    public boolean isUseGPS() { // Getter for useGPS
        return useGPS;
    }

    public void setUseGPS(boolean useGPS) { // Setter for useGPS
        this.useGPS = useGPS;
    }

    @NonNull
    public GoalType getGoalType() { // Getter for goal type
        return goalType;
    }

    public void setGoalType(@NonNull GoalType goalType) { // Setter for goal type
        this.goalType = goalType;
    }

    public boolean isNotificationsOn() { // Getter for notificationsOn
        return notificationsOn;
    }

    public void setNotificationsOn(boolean notificationsOn) { // Setter for notificationsOn
        this.notificationsOn = notificationsOn;
    }

    public Long getJoinDate() { // Getter for joinDate
        return joinDate;
    }

    public void setJoinDate(Long joinDate) { // Setter for joinDate
        this.joinDate = joinDate;
    }

}