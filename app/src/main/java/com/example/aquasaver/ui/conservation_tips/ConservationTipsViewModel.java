package com.example.aquasaver.ui.conservation_tips;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.aquasaver.model.Suggestions;

import java.util.List;

public class ConservationTipsViewModel extends ViewModel {

    private final MutableLiveData<List<Suggestions>> suggestionsLiveData = new MutableLiveData<>();

    public LiveData<List<Suggestions>> getSuggestionsLiveData() {
        return suggestionsLiveData;
    }

    public void loadSuggestions(String email) {
        List<Suggestions> allSuggestions = ConservationTipsSeeder.getConservationTips(email);
        suggestionsLiveData.setValue(allSuggestions);
    }
}
