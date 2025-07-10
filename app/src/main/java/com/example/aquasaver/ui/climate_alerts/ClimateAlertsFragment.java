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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentClimateAlertsBinding.inflate(inflater, container, false);
        climateAlertsViewModel = new ViewModelProvider(this).get(ClimateAlertsViewModel.class);

        climateAlertsViewModel.getSuggestionLiveData().observe(getViewLifecycleOwner(), suggestion -> {
            binding.textClimateAlerts.setText(suggestion);
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
