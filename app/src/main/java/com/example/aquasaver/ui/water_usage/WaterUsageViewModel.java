package com.example.aquasaver.ui.water_usage;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class WaterUsageViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public WaterUsageViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("");
    }

    public LiveData<String> getText() {
        return mText;
    }
}