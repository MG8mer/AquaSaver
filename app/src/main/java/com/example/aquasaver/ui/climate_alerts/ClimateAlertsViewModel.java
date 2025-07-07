package com.example.aquasaver.ui.climate_alerts;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ClimateAlertsViewModel extends ViewModel {
    private final MutableLiveData<String> mText;

    public ClimateAlertsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is climate alerts fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
