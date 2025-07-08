package com.example.aquasaver.model;

// Import important room db libraries
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

// Import SummaryType enum
import com.example.aquasaver.model.enums.SummaryType;

import java.util.Date;

@Entity(tableName = "reports",
        foreignKeys = @ForeignKey(entity = UserProfile.class,
                parentColumns = "email",
                childColumns = "user_email",
                onDelete = ForeignKey.CASCADE,
                onUpdate = ForeignKey.NO_ACTION),
        indices = {@Index(value = {"user_email"})})
public class Reports {

    @PrimaryKey(autoGenerate = true)
    public int id;

    // Instance variable for each reports table column
    @NonNull
    @ColumnInfo(name = "user_email")
    public String userEmail;

    @ColumnInfo(name = "summary_type")
    public SummaryType summaryType; // How the report is broken down (daily, weekly, monthly)

    @ColumnInfo(name = "start_date")
    public Date startDate; // Start date of report

    @ColumnInfo(name = "end_date")
    public Date endDate; // End date of report

    @ColumnInfo(name = "total_liters_used")
    public int totalLitersUsed; // Total liters of water used

    @ColumnInfo(name = "challenges_completed")
    public int challengesCompleted; // No. of fun "eco-challenges" completed

    @ColumnInfo(name = "streak_count")
    public int streakCount; // How many times user met water goal!

    // Constructor
    public Reports(@NonNull String userEmail,
                   SummaryType summaryType,
                   Date startDate,
                   Date endDate,
                   int totalLitersUsed,
                   int challengesCompleted,
                   int streakCount) {
        this.userEmail = userEmail;
        this.summaryType = summaryType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalLitersUsed = totalLitersUsed;
        this.challengesCompleted = challengesCompleted;
        this.streakCount = streakCount;
    }

    // Getters and setters for all instance variables (table columns)
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String email) { this.userEmail = email; }

    public SummaryType getSummaryType() { return summaryType; }
    public void setSummaryType(SummaryType summaryType) { this.summaryType = summaryType; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public int getTotalLitersUsed() { return totalLitersUsed; }
    public void setTotalLitersUsed(int totalLitersUsed) { this.totalLitersUsed = totalLitersUsed; }

    public int getChallengesCompleted() { return challengesCompleted; }
    public void setChallengesCompleted(int challengesCompleted) { this.challengesCompleted = challengesCompleted; }

    public int getStreakCount() { return streakCount; }
    public void setStreakCount(int streakCount) { this.streakCount = streakCount; }
}
