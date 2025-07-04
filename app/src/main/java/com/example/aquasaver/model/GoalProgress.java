package com.example.aquasaver.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "goal_progress",
        foreignKeys = @ForeignKey(entity = UserProfile.class, // Parent entity
                parentColumns = "email",     // Primary key in parent
                childColumns = "user_email", // Foreign key in this entity
                onDelete = ForeignKey.CASCADE, // Action on parent delete
                onUpdate = ForeignKey.NO_ACTION), // Action on parent update
        indices = {@Index(value = {"user_email"})}) // Index for the foreign key column
public class GoalProgress {

    @PrimaryKey(autoGenerate = true) // Auto-generating ID for log entries
    public int id;

    @NonNull
    @ColumnInfo(name = "user_email") // Foreign key part 1
    public String userEmail;

    @ColumnInfo(name = "amount_logged")
    public double amountLogged; // THIS IS THE SUM from WaterUsage

    @ColumnInfo(name = "progress_date") // Date this progress record was last updated or represents
    public Long progressDate; // Timestamp

    @ColumnInfo(name = "on_target")
    public boolean onTarget;

    // Constructor
    public GoalProgress(@NonNull String userEmail, double amountLogged, Long progressDate, boolean onTarget) {
        this.userEmail = userEmail;
        this.amountLogged = amountLogged;
        this.progressDate = progressDate;
        this.onTarget = onTarget;
    }


    public int getId() { // Getter for id
        return id;
    }

    @NonNull
    public String getUserEmail() { // Getter for userEmail
        return userEmail;
    }

    public void setUserEmail(@NonNull String userEmail) { // Setter for userEmail
        this.userEmail = userEmail;
    }

    public double getAmountLogged() { // Getter for amountLogged
        return amountLogged;
    }

    public void setAmountLogged(double amountLogged) { // Setter for amountLogged
        this.amountLogged = amountLogged;
    }

    public Long getProgressDate() { // Getter for progressDate
        return progressDate;
    }

    public void setProgressDate(Long progressDate) { // Setter for progressDate
        this.progressDate = progressDate;
    }

    public boolean getOnTarget() { // Getter for onTarget
        return onTarget;
    }

    public void setOnTarget(boolean onTarget) { // Setter for onTarget
        this.onTarget = onTarget;
    }

}