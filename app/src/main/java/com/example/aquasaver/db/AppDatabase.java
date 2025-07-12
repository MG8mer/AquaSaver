package com.example.aquasaver.db;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

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
        version = 3,
        exportSchema = true
)
@TypeConverters({Converters.class})
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

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // SQL for migrating from version 1 to 2
        }
    };

    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // SQL for migrating from version 2 to 3
        }
    };

    public static AppDatabase getInstance(Context context) {
        Log.d("AppDatabase", "getInstance called. Current INSTANCE: " + (INSTANCE == null ? "null" : "exists"));
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    Log.d("AppDatabase", "Creating new AppDatabase instance  for version 3 with MIGRATION_2_3.");
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "aqua_saver.db"
                            )
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                            .allowMainThreadQueries()
                            .build();
                    Log.d("AppDatabase", "AppDatabase instance built.");
                }
            }
        }
        return INSTANCE;
    }

}
