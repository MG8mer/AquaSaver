package com.example.aquasaver.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

// Import GoalProgress class
import com.example.aquasaver.model.GoalProgress;

import java.util.Date;
import java.util.List;
@Dao
public interface GoalProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertGoalProgress(GoalProgress goalProgress);

    @Update
    int updateGoalProgress(GoalProgress goalProgress);

    @Delete
    int deleteGoalProgress(GoalProgress goalProgress);

    @Query("DELETE FROM goal_progress WHERE user_email = :userEmail")
    int deleteAllProgressForUser(String userEmail);

    @Query("SELECT * FROM goal_progress WHERE user_email = :userEmail ORDER BY progress_date DESC")
    List<GoalProgress> getAllProgressForUser(String userEmail);

    @Query("SELECT * FROM goal_progress WHERE user_email = :userEmail AND progress_date >= :startDate AND progress_date <= :endDate ORDER BY progress_date DESC")
    List<GoalProgress> getProgressForUserInDateRange(String userEmail, long startDate, long endDate);

    @Query("SELECT * FROM goal_progress WHERE user_email = :userEmail AND on_target = :on_target")
    List<GoalProgress> getUserGoalProgressByCompletion(String userEmail, boolean on_target); // Returns the progress of all goals


    @Query("SELECT * FROM goal_progress WHERE user_email = :email AND DATE(progress_date / 1000, 'unixepoch') = DATE('now')")
    GoalProgress getTodayProgress(String email);

    @Query("SELECT * FROM goal_progress WHERE user_email = :email AND progress_date >= :startOfWeek AND progress_date < :endOfWeek LIMIT 1")
    GoalProgress getWeeklyProgress(String email, Date startOfWeek, Date endOfWeek);

    @Query("SELECT * FROM goal_progress WHERE user_email = :email AND progress_date >= :startOfMonth AND progress_date < :endOfMonth LIMIT 1")
    GoalProgress getMonthlyProgress(String email, Date startOfMonth, Date endOfMonth);


}