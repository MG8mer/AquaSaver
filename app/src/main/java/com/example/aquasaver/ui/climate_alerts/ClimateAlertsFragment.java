package com.example.aquasaver.ui.climate_alerts;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.aquasaver.databinding.FragmentClimateAlertsBinding;

import java.util.Map;

public class ClimateAlertsFragment extends Fragment {

    private FragmentClimateAlertsBinding binding;
    private ClimateAlertsViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("ClimateAlertsFragment", "onCreateView started");
        binding = FragmentClimateAlertsBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(ClimateAlertsViewModel.class);
        binding.textClimateAlerts.setText("Fragment loaded"); // before SharedPreferences

        // Get stored user info
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
        Map<String, ?> allEntries = prefs.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Log.d("PrefsDump", entry.getKey() + ": " + entry.getValue().toString());
        }
        String email = prefs.getString("username", null);
        String location = prefs.getString("location", null);

        Log.d("ClimateAlertsFragment", "Fetched from prefs: username=" + email + ", location=" + location);

        if (email == null || location == null) {
            Toast.makeText(getContext(), "User not logged in or location missing", Toast.LENGTH_LONG).show();
            binding.textClimateAlerts.setText("Please log in and set your location.");
            Log.w("ClimateAlertsFragment", "Missing user info: email=" + email + ", location=" + location);
        } else {
            viewModel.loadSmartSuggestions(requireContext(), email, location);
            Log.d("ClimateAlertsFragment", "User info loaded: email=" + email + ", location=" + location);
        }

        // Observe LiveData from ViewModel and update UI
        viewModel.getSuggestionLiveData().observe(getViewLifecycleOwner(), suggestions -> {
            Log.d("ClimateAlertsFragment", "Suggestions received: " + suggestions);
            if (suggestions != null && !suggestions.isEmpty()) {
                binding.textClimateAlerts.setText(suggestions);
            } else {
                binding.textClimateAlerts.setText("No suggestions available.");
                Log.d("ClimateAlertsFragment", "No suggestions received");
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
