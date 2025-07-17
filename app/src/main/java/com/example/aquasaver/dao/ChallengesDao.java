package com.example.aquasaver.dao;

// import important room db libraries

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

// Import Challenges class
import com.example.aquasaver.challenges.ChallengeWithProgress;
import com.example.aquasaver.model.Challenges;

// Import list
import java.util.List;

@Dao
public interface ChallengesDao
{
    /*

     Insert queries

     */

    @Insert(onConflict = OnConflictStrategy.REPLACE) // insert one eco-challenge
    long insertChallenge(Challenges challenge); // returns ID of newly added eco-challenge

    @Insert(onConflict = OnConflictStrategy.REPLACE) // insert many eco-challenges
    void insertAllChallenges(List<Challenges> challenge);

    // Update queries
    @Update
    void updateChallenge(Challenges challenge); // updates a specific eco-challenge

    /*

    Delete queries

     */
    @Delete
    void deleteChallenge(Challenges challenge); // deletes a specific eco-challenge

    @Query("DELETE FROM challenges WHERE user_email = :userEmail")
    int deleteUserChallenges(String userEmail); // deletes all user eco-challenges and return # rows deleted

    /*

    Select queries

     */

    @Query("SELECT * FROM challenges WHERE id = :challengeId LIMIT 1")
    Challenges getChallengeById(int challengeId); // Returns a specific eco-challenge by Id

    @Query("SELECT * FROM challenges WHERE user_email = :userEmail")
    List<Challenges> getUserChallenges(String userEmail); // Returns all the eco-challenges of a particular user

    @Query("SELECT * FROM challenges WHERE user_email = :userEmail AND start_date <= :currentDate AND end_date >= :currentDate")
    List<Challenges> getUserChallengesByTime(String userEmail, long currentDate); // Returns all eco-challenges of a particular user at a specific time

    @Transaction
    @Query("SELECT * FROM Challenges WHERE user_email = :userEmail")
    List<ChallengeWithProgress> getAllChallengesWithProgress(String userEmail);

    @Query("SELECT COUNT(*) FROM Challenges WHERE user_email = :email")
    int countChallengesForUser(String email);
    @Query("SELECT * FROM challenges WHERE title = :title LIMIT 1")
    Challenges getChallengeByTitle(String title);

    @Query("SELECT * FROM Challenges WHERE user_email = :userEmail AND goal_amount == 1 AND end_date <= :currentDate")
    List<Challenges> getOneDayChallengesBeforeDate(String userEmail, Long currentDate); // Returns all eco-challenges that ended before a certain date and are one day long

    @Query("SELECT * FROM Challenges WHERE user_email = :userEmail AND goal_amount > 1 AND goal_amount <= 7 AND end_date <= :currentDate")
    List<Challenges> getWeeklyChallengesBeforeDate(String userEmail, Long currentDate); // Returns all eco-challenges that ended before a certain date and are at most a week long and at least two days long

    @Query("SELECT * FROM Challenges WHERE user_email = :userEmail AND goal_amount > 7 AND goal_amount <= 28 AND end_date <= :currentDate")
    List<Challenges> getMonthlyChallengesBeforeDate(String userEmail, Long currentDate); // Returns all eco-challenges that ended before a certain date and are at most a month long (28 days) and at least 8 days long
}