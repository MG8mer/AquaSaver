package com.example.aquasaver.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.aquasaver.model.*;  // model --> houses all database entities
import com.example.aquasaver.dao.*;    // DAO (database operations) --> houses files storing database operations for each entity

@Database(
        entities = {
                UserProfile.class,
                WaterUsage.class,
                GoalProgress.class,
                Alerts.class,
                WeatherSuggestions.class,
                Reports.class,
                Challenges.class,
                ChallengeProgress.class
        },
        version = 1,
        exportSchema = true
)
public abstract class AppDatabase extends RoomDatabase {

    // ACCESSORS FOR DAO FILES FOR EACH ENTITY
    public abstract UserProfileDao          userProfileDao();
    public abstract WaterUsageDao           waterUsageDao();
    public abstract GoalProgressDao         goalProgressDao();
    public abstract AlertsDao                alertsDao();
    public abstract WeatherSuggestionsDao    weatherSuggestionsDao();
    public abstract ReportsDao               reportsDao();
    public abstract ChallengesDao            challengesDao();
    public abstract ChallengeProgressDao    challengeProgressDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "aqua_saver.db").build();
                }
            }
        }
        return INSTANCE;
    }
}