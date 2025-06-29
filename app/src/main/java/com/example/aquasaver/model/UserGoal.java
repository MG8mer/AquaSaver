package com.example.aquasaver.model;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "user_goals",
        foreignKeys = @ForeignKey(entity = UserProfile.class,
                parentColumns = "email",
                childColumns = "user_email",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("user_email")})
public class UserGoal {
    @PrimaryKey(autoGenerate = true)
    public int id; //Referenced by GoalProgress.userGoalId

    @NonNull
    @ColumnInfo(name = "user_email", index = true)
    public String userEmail;

    @NonNull
    @ColumnInfo(name = "goal_id") // Goal identifier
    public String goalID;

    @ColumnInfo(name = "target_amount_liters") // The actual goal value to compare against
    public double targetAmountLiters;

    @ColumnInfo(name = "start_date")
    public Long startDate; // Timestamp

    @ColumnInfo(name = "end_date")
    public Long endDate; // Timestamp

    @ColumnInfo(name = "activity_type_filter") // Optional: e.g., "Shower", "All", "Gardening"
    public String activityTypeFilter;

    public UserGoal(@NonNull String userEmail, @NonNull String goalID, double targetAmountLiters,
                    Long startDate, Long endDate, String activityTypeFilter) { // Constructor
        this.userEmail = userEmail;
        this.goalID = goalID;
        this.targetAmountLiters = targetAmountLiters;
        this.startDate = startDate;
        this.endDate = endDate;
        this.activityTypeFilter = activityTypeFilter;
    }

    @NonNull
    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(@NonNull String userEmail) {
        this.userEmail = userEmail;
    }

    @NonNull
    public String getGoalID() {
        return goalID;
    }

    public void setGoalID(@NonNull String goalID) {
        this.goalID = goalID;
    }

    public double getTargetAmountLiters() {
        return targetAmountLiters;
    }

    public void setTargetAmountLiters(double targetAmountLiters) {
        this.targetAmountLiters = targetAmountLiters;
    }

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public Long getEndDate() {
        return endDate;
    }

    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }

    public String getActivityTypeFilter() {
        return activityTypeFilter;
    }

    public void setActivityTypeFilter(String activityTypeFilter) {
        this.activityTypeFilter = activityTypeFilter;
    }
}

