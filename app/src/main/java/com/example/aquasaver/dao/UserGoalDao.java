package com.example.aquasaver.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserGoalDao {
/*
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertUserGoal(UserGoal userGoal);

    @Update
    int updateUserGoal(UserGoal userGoal);

    @Delete
    int deleteUserGoal(UserGoal userGoal);

    @Query("DELETE FROM user_goals WHERE id = :goalId")
    int deleteUserGoalById(int goalId);

    @Query("DELETE FROM user_goals WHERE user_email = :userEmail")
    int deleteUserGoalsForUser(String userEmail); //deletes all user goals

    @Query("SELECT * FROM user_goals WHERE id = :goalId LIMIT 1")
    UserGoal getUserGoalById(int goalId); //retrieves specific goal

    @Query("SELECT * FROM user_goals WHERE user_email = :userEmail")
    List<UserGoal> getUserGoalsByEmail(String userEmail); //retrieves all goals

    @Query("SELECT * FROM user_goals WHERE user_email = :userEmail AND goal_name = :goalName LIMIT 1")
    UserGoal getUserGoalByName(String userEmail, String goalName);

    @Query("SELECT * FROM user_goals WHERE user_email = :userEmail AND start_date <= :currentDate AND end_date >= :currentDate")
    List<UserGoal> getActiveUserGoals(String userEmail, long currentDate);

    @Query("SELECT * FROM user_goals")
    List<UserGoal> getAllUserGoals();

    @Query("SELECT EXISTS(SELECT 1 FROM user_goals WHERE id = :goalId LIMIT 1)")
    boolean doesGoalExist(int goalId);

 */
}