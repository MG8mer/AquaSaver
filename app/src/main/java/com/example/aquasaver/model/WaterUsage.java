package com.example.aquasaver.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "water_usage_log",
        foreignKeys = @ForeignKey(entity = UserProfile.class, // Parent entity
                parentColumns = "email",     // Primary key in parent
                childColumns = "user_email", // Foreign key in this entity
                onDelete = ForeignKey.CASCADE, // Action on parent delete
                onUpdate = ForeignKey.NO_ACTION), // Action on parent update
        indices = {@Index(value = {"user_email"})}) // Index for the foreign key column
public class WaterUsage {

    @PrimaryKey(autoGenerate = true) // Auto-generating ID for log entries
    public int id;

    @ColumnInfo(name = "user_email") // Foreign key to UserProfile.email
    @NonNull
    public String userEmail;

    @ColumnInfo(name = "usage_date")
    public Long usageDate;

    @ColumnInfo(name = "amount_liters")
    public double amountLiters;

    @ColumnInfo(name = "activity_type") // e.g., "Shower", "Dishwasher", "Gardening"
    public String activityType;

    public WaterUsage(@NonNull String userEmail, Long usageDate, double amountLiters, String activityType) { // Constructor
        this.userEmail = userEmail;
        this.usageDate = usageDate;
        this.amountLiters = amountLiters;
        this.activityType = activityType;
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

    public Long getUsageDate() { // Getter for usageDate
        return usageDate;
    }

    public void setUsageDate(Long usageDate) { // Setter for usageDate
        this.usageDate = usageDate;
    }

    public double getAmountLiters() { // Getter for amountLiters
        return amountLiters;
    }

    public void setAmountLiters(double amountLiters) { // Setter for amountLiters
        this.amountLiters = amountLiters;
    }

    public String getActivityType() { // Getter for activityType
        return activityType;
    }

    public void setActivityType(String activityType) { // Setter for activityType
        this.activityType = activityType;
    }
}