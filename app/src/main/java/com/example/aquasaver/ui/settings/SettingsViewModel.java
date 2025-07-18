package com.example.aquasaver.ui.settings;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.aquasaver.db.AppDatabase;
import com.example.aquasaver.model.UserProfile;

public class SettingsViewModel extends AndroidViewModel {

    private final AppDatabase db;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
    }

    public UserProfile getUserProfileByEmail(String email) {
        return db.userProfileDao().getUserByEmail(email);
    }

    // Updated to return int so caller knows rows updated
    public int updateUserProfile(UserProfile userProfile) {
        return db.userProfileDao().updateUserProfile(userProfile);
    }
}
