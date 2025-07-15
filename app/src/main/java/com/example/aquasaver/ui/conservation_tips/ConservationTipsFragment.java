package com.example.aquasaver.ui.conservation_tips;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.aquasaver.R;
import com.example.aquasaver.databinding.FragmentConservationTipsBinding;
import com.example.aquasaver.smart_suggestions.weatherapi.WeatherRepository;

import android.text.Html;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConservationTipsFragment extends Fragment {

    private FragmentConservationTipsBinding binding;
    private ConservationTipsViewModel viewModel;
    TextView temperatureText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentConservationTipsBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(ConservationTipsViewModel.class);

        // Get user preferences
        SharedPreferences userPrefs = requireActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
        String email = userPrefs.getString("username", null);
        String location = userPrefs.getString("location", null);

        if (email == null || location == null) {
            Toast.makeText(getContext(), "User not logged in or location missing", Toast.LENGTH_LONG).show();
            binding.textConservationTips.setText("Please log in and set your location.");
            Log.w("ConservationTipsFragment", "Missing user info: email=" + email + ", location=" + location);
            return binding.getRoot();
        }

        // Smart Suggestions Logic
        viewModel.loadSmartSuggestions(requireContext(), email, location);
        viewModel.getSuggestionLiveData().observe(getViewLifecycleOwner(), suggestions -> {
            if (suggestions != null && !suggestions.isEmpty()) {
                // Extract just the 5 numbered tips using regex
                List<String> tips = new ArrayList<>();
                TextView[] alertTitles = {
                        binding.alertTitle1,
                        binding.alertTitle2,
                        binding.alertTitle3,
                        binding.alertTitle4,
                        binding.alertTitle5
                };
                TextView[] alertTexts = {
                        binding.alertText1,
                        binding.alertText2,
                        binding.alertText3,
                        binding.alertText4,
                        binding.alertText5
                };
                ConstraintLayout[] alertBoxes = {
                        binding.alertBox1,
                        binding.alertBox2,
                        binding.alertBox3,
                        binding.alertBox4,
                        binding.alertBox5
                };

                int index = 0;
                Matcher matcher = Pattern.compile("(?m)^\\d+\\.\\s\\*\\*(.*?)\\*\\*:?\\s*(.*?)(?=^\\d+\\.\\s\\*\\*|\\z)", Pattern.DOTALL)
                        .matcher(suggestions);

                while (matcher.find() && index < alertTitles.length) {
                    String title = matcher.group(1).trim();
                    String body = matcher.group(2).trim();

                    alertTitles[index].setText("💧 " + title);
                    alertTexts[index].setText(body);
                    alertBoxes[index].setVisibility(View.VISIBLE);
                    index++;
                }

                for (int i = index; i < alertBoxes.length; i++) {
                    alertBoxes[i].setVisibility(View.GONE);
                }
            } else {
                binding.alertText1.setText("No suggestions available.");
                binding.alertText2.setText("");
                binding.alertText3.setText("");
                binding.alertText4.setText("");
                binding.alertText5.setText("");
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
