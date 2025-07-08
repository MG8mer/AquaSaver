package com.example.aquasaver.ui.climate_alerts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.aquasaver.databinding.FragmentClimateAlertsBinding;

public class ClimateAlertsFragment extends Fragment {

    private FragmentClimateAlertsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ClimateAlertsViewModel climateAlertsViewModel =
                new ViewModelProvider(this).get(ClimateAlertsViewModel.class);

        binding = FragmentClimateAlertsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textClimateAlerts;
        climateAlertsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
