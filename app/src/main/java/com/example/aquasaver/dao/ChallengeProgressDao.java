package com.example.aquasaver.dao;

// import important room db libraries

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

// Import Challenges class
import com.example.aquasaver.model.ChallengeProgress;
import com.example.aquasaver.model.Challenges;

// Import list
import java.util.List;

@Dao
public interface ChallengeProgressDao
{
    /*

     Insert queries

     */

    @Insert(onConflict = OnConflictStrategy.REPLACE) // insert an eco-challenge progress
    long insertChallengeProgress(ChallengeProgress challengeProgress); // returns ID of newly added eco-challenge progress

    @Insert(onConflict = OnConflictStrategy.REPLACE) // insert many eco-challenge progresses
    void insertAllChallengeProgress(List<ChallengeProgress> challengeProgress);

    // Update queries
    @Update
    void updateChallengeProgress(ChallengeProgress challengeProgress); // updates the progress of an eco-challenge

    /*

    Delete queries

     */
    @Delete
    void deleteChallengeProgress(ChallengeProgress challengeProgress); // deletes the progress a specific eco-challenge

    @Query("DELETE FROM challenge_progress WHERE user_email = :userEmail")
    int deleteChallengeProgress(String userEmail); // deletes the progress of all user eco-challenges and return # rows deleted

    /*

    Select queries

     */

    @Query("SELECT * FROM challenge_progress WHERE title = :title LIMIT 1")
    ChallengeProgress getChallengeProgressById(String title); // Returns the progress of a specific eco-challenge by Id
    @Query("SELECT * FROM challenge_progress WHERE title = :title AND user_email = :email")
    ChallengeProgress getChallengeProgressById(String title, String email);

    @Query("SELECT * FROM challenge_progress WHERE user_email = :userEmail AND title = :title LIMIT 1")
    ChallengeProgress getChallengeProgressByUserEmailAndTitle(String userEmail, String title);

    @Query("SELECT * FROM challenge_progress WHERE user_email = :userEmail")
    List<ChallengeProgress> getUserChallengeProgress(String userEmail); // Returns the progress of all eco-challenges of a particular user

    @Query("SELECT * FROM challenge_progress WHERE user_email = :userEmail AND completion = :completion")
    List<ChallengeProgress> getUserChallengeProgressByCompletion(String userEmail, boolean completion); // Returns the progress of all eco-challenges of a particular user by whether they are or not completed

    @Query("SELECT * FROM challenge_progress WHERE title = :title LIMIT 1")
    ChallengeProgress getChallengeByTitle(String title);

    @Query("SELECT * FROM challenge_progress WHERE user_email = :email AND date(timestamp / 1000, 'unixepoch') = date('now')")
    ChallengeProgress getTodayChallengeProgress(String email);

    @Query("SELECT * FROM challenge_progress WHERE user_email = :email AND completion = 1")
    List<ChallengeProgress> getCompletedChallengesByUser(String email);
}