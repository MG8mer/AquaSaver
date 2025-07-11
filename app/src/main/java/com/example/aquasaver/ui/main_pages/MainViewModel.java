package com.example.aquasaver.ui.main_pages;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.aquasaver.db.AppDatabase;

public class MainViewModel extends AndroidViewModel {

    private final AppDatabase db;

    public MainViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application); // Singleton pattern
    }

}
