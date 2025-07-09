package com.example.aquasaver.ui.climate_alerts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.aquasaver.R;
import com.example.aquasaver.databinding.FragmentClimateAlertsBinding;

public class ClimateAlertsFragment extends Fragment {

    private FragmentClimateAlertsBinding binding;
    private ClimateAlertsViewModel climateAlertsViewModel;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvSuggestions = view.findViewById(R.id.text_climate_alerts);

        tvSuggestions.setText("Loading smart suggestions..."); //TEST
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_climate_alerts, container, false);

        climateAlertsViewModel = new ViewModelProvider(this).get(ClimateAlertsViewModel.class);
        TextView text_climate_alerts = view.findViewById(R.id.text_climate_alerts);

        climateAlertsViewModel.getSuggestionLiveData().observe(getViewLifecycleOwner(), suggestion -> {
            text_climate_alerts.setText(suggestion);
        });

        String weatherInfo = "Today's weather is + [temp] and [condition] in [location]. Suggest smart water-saving tips."; //TODO: PASS IN REAL DATA
        climateAlertsViewModel.fetchSuggestion(weatherInfo);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
