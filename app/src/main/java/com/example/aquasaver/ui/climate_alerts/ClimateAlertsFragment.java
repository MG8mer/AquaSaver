package com.example.aquasaver.ui.climate_alerts;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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


        SharedPreferences sharedPrefs = requireContext().getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
        String location = sharedPrefs.getString("location", null);



        climateAlertsViewModel.setLocation(location);

        climateAlertsViewModel.getLocation().observe(getViewLifecycleOwner(), loc -> {
            binding.textLocation.setText(loc != null ? loc : "No location set");
        });


        climateAlertsViewModel.getSuggestionLiveData().observe(getViewLifecycleOwner(), suggestion -> {
            binding.textClimateAlerts.setText(suggestion);

        });





        Context context = getContext();
        if (context != null) {
            Drawable drawable = ContextCompat.getDrawable(context, R.drawable.rounded_bg);
            if (drawable != null) {
                drawable = drawable.mutate();
                drawable.setTint(Color.parseColor("#DC2626"));

                binding.alertBox1.setBackground(drawable);
            }

            Drawable drawable1 = ContextCompat.getDrawable(context, R.drawable.rounded_bg);
            if (drawable1 != null) {
                drawable1 = drawable1.mutate();
                drawable1.setTint(Color.parseColor("#FBBF24"));
                binding.alertBox2.setBackground(drawable1);
            }
        }

        return binding.getRoot();


    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
