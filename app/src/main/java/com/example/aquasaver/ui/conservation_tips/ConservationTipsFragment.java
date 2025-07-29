package com.example.aquasaver.ui.conservation_tips;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.aquasaver.R;
import com.example.aquasaver.databinding.FragmentConservationTipsBinding;
import com.example.aquasaver.model.Suggestions;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConservationTipsFragment extends Fragment {

    private FragmentConservationTipsBinding binding;
    private ConservationTipsViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentConservationTipsBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(ConservationTipsViewModel.class);

        SharedPreferences prefs = requireActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
        String email = prefs.getString("username", null);

        if (email == null) {
            Toast.makeText(getContext(), "Please log in to see suggestions.", Toast.LENGTH_LONG).show();
            binding.conservationTipsLabel.setText("User not logged in.");
            return binding.getRoot();
        }

        viewModel.loadSuggestions(email);
        viewModel.getSuggestionsLiveData().observe(getViewLifecycleOwner(), this::displaySuggestions);

        return binding.getRoot();
    }

    private void displaySuggestions(List<Suggestions> suggestions) {
        LinearLayout container = binding.conservationTipsContainer;
        container.removeAllViews();

        if (suggestions == null || suggestions.isEmpty()) {
            TextView emptyText = new TextView(requireContext());
            emptyText.setText("No tips available.");
            emptyText.setTextSize(16);
            emptyText.setPadding(8, 8, 8, 8);
            container.addView(emptyText);
            return;
        }

        Map<String, List<Suggestions>> grouped = suggestions.stream()
                .collect(Collectors.groupingBy(Suggestions::getCondition));

        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (Map.Entry<String, List<Suggestions>> entry : grouped.entrySet()) {
            String condition = entry.getKey();
            List<Suggestions> tips = entry.getValue();

            // Add section header
            TextView sectionHeader = new TextView(requireContext());
            sectionHeader.setText("Tips for " + condition + " Weather:");
            sectionHeader.setTextSize(18);
            sectionHeader.setTypeface(null, Typeface.BOLD);
            sectionHeader.setTextColor(Color.parseColor("#3F51B5"));
            sectionHeader.setPadding(0, 24, 0, 8);
            container.addView(sectionHeader);

            // Add each tip as an alert box
            for (Suggestions tip : tips) {
                View alertBox = inflater.inflate(R.layout.item_alert_box, container, false);
                TextView title = alertBox.findViewById(R.id.alertTitle);
                TextView text = alertBox.findViewById(R.id.alertText);

                title.setText(tip.getTitle());
                text.setText(tip.getDescription());

                container.addView(alertBox);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
