package com.example.aquasaver.model;

// Import important room db libraries
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.aquasaver.model.enums.ChallengeGoalType;


@Entity(tableName = "challenges",
        foreignKeys = @ForeignKey(entity = UserProfile.class,
                parentColumns = "email",
                childColumns = "user_email",
                onDelete = ForeignKey.CASCADE,
                onUpdate = ForeignKey.NO_ACTION),
        indices = {@Index(value = {"user_email"})})
public class Challenges {

    @PrimaryKey(autoGenerate = true)
    public int id;

    // Instance variables for columns of challenges db table/entity
    @NonNull
    @ColumnInfo(name = "user_email")
    public String userEmail;

    @ColumnInfo(name = "title")
    public String title; // challenge title

    @ColumnInfo(name = "description")
    public String description; // challenge description

    @ColumnInfo(name = "goal_type")
    public ChallengeGoalType goalType; // user's goal type (daily/weekly challenges)

    @ColumnInfo(name = "goal_amount")
    public int goalAmount; // amount of goals user has

    @ColumnInfo(name = "start_date")
    public Long startDate; // start of challenge

    @ColumnInfo(name = "end_date")
    public Long endDate; // end of challenge

    // Constructor
    public Challenges(@NonNull String userEmail,
                      String title,
                      String description,
                      ChallengeGoalType goalType,
                      int goalAmount,
                      Long startDate,
                      Long endDate) {
        this.userEmail = userEmail;
        this.title = title;
        this.description = description;
        this.goalType = goalType;
        this.goalAmount = goalAmount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and setters for each instance variable
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ChallengeGoalType getGoalType() { return goalType; }
    public void setGoalType(ChallengeGoalType goalType) { this.goalType = goalType; }

    public int getGoalAmount() { return goalAmount; }
    public void setGoalAmount(int goalAmount) { this.goalAmount = goalAmount; }

    public Long getStartDate() { return startDate; }
    public void setStartDate(Long startDate) { this.startDate = startDate; }

    public Long getEndDate() { return endDate; }
    public void setEndDate(Long endDate) { this.endDate = endDate; }
}
