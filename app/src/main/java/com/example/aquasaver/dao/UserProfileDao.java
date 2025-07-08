package com.example.aquasaver.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;


// Import user profile class
import com.example.aquasaver.model.UserProfile;

import java.util.List;

@Dao
public interface UserProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertUserProfile(UserProfile userProfile);

    @Update
    int updateUserProfile(UserProfile userProfile);

    @Delete
    int deleteUserProfile(UserProfile userProfile);

    @Query("DELETE FROM user_profiles WHERE email = :email AND location = :location")
    int deleteUserProfileByIds(String email, String location); //Deletes a user profile based on email and location and returns # of rows deleted

    @Query("SELECT * FROM user_profiles WHERE email = :email AND location = :location LIMIT 1")
    UserProfile getUserProfileByIds(String email, String location); //Retrieves a specific user profile by its composite primary key (email and location).

    @Query("SELECT * FROM user_profiles WHERE email = :email")
    List<UserProfile> getUserProfilesByEmail(String email); // Returns a list of all user profiles associated with a given email

    @Query("SELECT * FROM user_profiles")
    List<UserProfile> getAllUserProfiles();

    @Query("SELECT EXISTS(SELECT 1 FROM user_profiles WHERE email = :email AND location = :location LIMIT 1)")
    boolean doesProfileExist(String email, String location);
}