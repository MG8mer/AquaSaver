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

import com.example.aquasaver.challenges.ChallengeSeeder;
import com.example.aquasaver.model.*;
import com.example.aquasaver.dao.*;

import java.util.List;
import java.util.concurrent.Executors;

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
        version = 8,
        exportSchema = false
)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    // DAO accessors
    public abstract UserProfileDao userProfileDao();
    public abstract WaterUsageDao waterUsageDao();
    public abstract GoalProgressDao goalProgressDao();
    public abstract AlertsDao alertsDao();
    public abstract WeatherSuggestionsDao weatherSuggestionsDao();
    public abstract ReportsDao reportsDao();
    public abstract ChallengesDao challengesDao();
    public abstract ChallengeProgressDao challengeProgressDao();

    private static volatile AppDatabase INSTANCE;

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Example: Add real SQL if needed for upgrade from version 1 to 2
        }
    };

    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Example: Add real SQL if needed for upgrade from version 2 to 3
        }
    };

    public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE user_profiles ADD COLUMN streak INTEGER NOT NULL DEFAULT 0");
        }
    };

    public static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Store Date as INTEGER (timestamp), and use correct column name
            database.execSQL("ALTER TABLE user_profiles ADD COLUMN last_streak_update INTEGER");
        }
    };
    static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Add the missing goal_amount column
            database.execSQL("ALTER TABLE goal_progress ADD COLUMN goal_amount INTEGER NOT NULL DEFAULT 100");
        }
    };

    static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Create ChallengeProgress table including the timestamp column
            database.execSQL("CREATE TABLE IF NOT EXISTS ChallengeProgress (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "challengeId INTEGER NOT NULL, " +
                    "userId INTEGER NOT NULL, " +
                    "progress INTEGER NOT NULL, " +
                    "timestamp INTEGER NOT NULL DEFAULT 0)");
        }
    };
    static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Make sure table name matches your Room table name: "challenge_progress"
            database.execSQL("ALTER TABLE challenge_progress ADD COLUMN timestamp INTEGER");
        }
    };



    // Returns the singleton instance
    public static AppDatabase getInstance(Context context) {
        Log.d("AppDatabase", "getInstance called. Current INSTANCE: " + (INSTANCE == null ? "null" : "exists"));
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "aqua_saver.db"
                            )
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                            .build();
                    Log.d("AppDatabase", "AppDatabase instance built.");
                }
            }
        }
        return INSTANCE;
    }
}
