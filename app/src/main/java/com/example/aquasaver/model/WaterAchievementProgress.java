package com.example.aquasaver.model;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "water_achievement_progress",
        foreignKeys = @ForeignKey(entity = UserProfile.class, // Parent entity
                parentColumns = "email",     // Primary key in parent
                childColumns = "user_email", // Foreign key in this entity
                onDelete = ForeignKey.CASCADE, // Action on parent delete
                onUpdate = ForeignKey.NO_ACTION), // Action on parent update
        indices = {@Index(value = {"user_email"})}) // Index for the foreign key column
public class WaterAchievementProgress {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    @ColumnInfo(name = "user_email", index = true)
    public String userEmail;

    @ColumnInfo(name = "on_target") //// This links to the specific onTarget definition being tracked
    public boolean onTarget;

    @ColumnInfo(name = "streak")
    public int streak; // e.g., number of consecutive days/weeks on target

    @NonNull
    @ColumnInfo(name = "date_earned")
    public Long dateEarned; // Store as Long (timestamp) for simplicity

    // Constructor
    public WaterAchievementProgress(@NonNull String userEmail, boolean onTarget, int streak,
                                    @NonNull Long dateEarned) {
        this.userEmail = userEmail;
        this.onTarget = onTarget;
        this.streak = streak;
        this.dateEarned = dateEarned;
    }

    public int getId() {
        return id;
    }

    @NonNull
    public String getUserEmail() {
        return userEmail;
    }
    public void setUserEmail(@NonNull String userEmail) {
        this.userEmail = userEmail;
    }

    public boolean isOnTarget() {
        return onTarget;
    }
    public void setOnTarget(boolean onTarget) {
        this.onTarget = onTarget;
    }

    public int getStreak() {
        return streak;
    }
    public void setStreak(int streak) {
        this.streak = streak;
    }

    @NonNull
    public Long getDateEarned() {
        return dateEarned;
    }
    public void setDateEarned(@NonNull Long dateEarned) {
        this.dateEarned = dateEarned;
    }
}
