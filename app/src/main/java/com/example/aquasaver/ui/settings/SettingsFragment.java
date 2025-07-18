package com.example.aquasaver.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.aquasaver.R;
import com.example.aquasaver.model.UserProfile;
import com.example.aquasaver.model.enums.GoalType;

public class SettingsFragment extends Fragment {

    private SettingsViewModel viewModel;

    private TextView textUserEmail;
    private SwitchCompat switchNotifications, switchGPS, switchWeatherAlerts;
    private Button buttonSave;

    private EditText editCurrentPassword, editNewPassword, editLocation;
    private Spinner spinnerGoalType;

    private UserProfile userProfile;

    private String userEmail;  // will hold the logged-in user email

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize views
        textUserEmail = view.findViewById(R.id.text_user_email);

        switchNotifications = view.findViewById(R.id.switch_notifications);
        switchGPS = view.findViewById(R.id.switch_gps);
        switchWeatherAlerts = view.findViewById(R.id.switch_weather_alerts);
        buttonSave = view.findViewById(R.id.button_save_settings);

        editCurrentPassword = view.findViewById(R.id.edit_current_password);
        editNewPassword = view.findViewById(R.id.edit_new_password);
        editNewPassword.setEnabled(false); // Disabled until current password is verified
        editLocation = view.findViewById(R.id.edit_location);
        spinnerGoalType = view.findViewById(R.id.spinner_goal_type);

        // Setup spinner adapter
        ArrayAdapter<GoalType> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, GoalType.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGoalType.setAdapter(adapter);

        viewModel = new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication())
                .create(SettingsViewModel.class);

        // Load logged-in user email from SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
        userEmail = prefs.getString("username", null);

        if (userEmail == null) {
            textUserEmail.setText("No user logged in");
            Toast.makeText(requireContext(), "No logged-in user found", Toast.LENGTH_SHORT).show();
            // Optionally disable UI here if needed
            return view;
        }

        // Load user profile with real email
        loadUserProfile(userEmail);

        // Set toggle switch colors initially and on changes
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> updateSwitchColor(switchNotifications));
        switchGPS.setOnCheckedChangeListener((buttonView, isChecked) -> updateSwitchColor(switchGPS));
        switchWeatherAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> updateSwitchColor(switchWeatherAlerts));

        // Listen to current password input changes to verify and enable new password
        editCurrentPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (userProfile == null) {
                    editNewPassword.setEnabled(false);
                    return;
                }
                String input = s.toString().trim();
                if (input.equals(userProfile.getPasswordHash())) {
                    editNewPassword.setEnabled(true);
                } else {
                    editNewPassword.setEnabled(false);
                    editNewPassword.setText("");
                }
            }
            @Override public void afterTextChanged(Editable s) { }
        });

        buttonSave.setOnClickListener(v -> {
            if (userProfile == null) {
                Toast.makeText(requireContext(), "User profile not loaded", Toast.LENGTH_SHORT).show();
                return;
            }

            String currentPwdInput = editCurrentPassword.getText().toString().trim();
            boolean isCurrentPwdCorrect = !TextUtils.isEmpty(currentPwdInput) && currentPwdInput.equals(userProfile.getPasswordHash());

            // If new password is entered, current password must be correct
            if (!TextUtils.isEmpty(editNewPassword.getText().toString().trim()) && !isCurrentPwdCorrect) {
                Toast.makeText(requireContext(), "Current password incorrect", Toast.LENGTH_SHORT).show();
                return;
            }

            userProfile.setNotificationsOn(switchNotifications.isChecked());
            userProfile.setUseGPS(switchGPS.isChecked());
            userProfile.setWeatherAlertsEnabled(switchWeatherAlerts.isChecked());

            if (isCurrentPwdCorrect && !TextUtils.isEmpty(editNewPassword.getText().toString().trim())) {
                userProfile.setPasswordHash(editNewPassword.getText().toString().trim());
            }

            String locationInput = editLocation.getText().toString().trim();
            if (!locationInput.isEmpty()) {
                userProfile.setLocation(locationInput);
            }

            GoalType selectedGoal = (GoalType) spinnerGoalType.getSelectedItem();
            userProfile.setGoalType(selectedGoal);

            // Update DB in background thread
            new Thread(() -> {
                int updated = viewModel.updateUserProfile(userProfile);
                requireActivity().runOnUiThread(() -> {
                    if (updated > 0) {
                        Toast.makeText(requireContext(), "Settings updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        });

        return view;
    }

    private void loadUserProfile(String email) {
        new Thread(() -> {
            userProfile = viewModel.getUserProfileByEmail(email);
            if (userProfile != null) {
                requireActivity().runOnUiThread(() -> {
                    // Display user email
                    textUserEmail.setText(userProfile.getEmail());

                    switchNotifications.setChecked(userProfile.isNotificationsOn());
                    updateSwitchColor(switchNotifications);

                    switchGPS.setChecked(userProfile.isUseGPS());
                    updateSwitchColor(switchGPS);

                    switchWeatherAlerts.setChecked(userProfile.isWeatherAlertsEnabled());
                    updateSwitchColor(switchWeatherAlerts);

                    editLocation.setText(userProfile.getLocation());

                    GoalType[] goals = GoalType.values();
                    for (int i = 0; i < goals.length; i++) {
                        if (goals[i] == userProfile.getGoalType()) {
                            spinnerGoalType.setSelection(i);
                            break;
                        }
                    }
                });
            } else {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "User profile not found", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void updateSwitchColor(SwitchCompat sw) {
        int onColor = getResources().getColor(R.color.colorPrimary);
        int offColor = getResources().getColor(android.R.color.darker_gray);

        if (sw.isChecked()) {
            sw.getThumbDrawable().setTint(onColor);
            sw.getTrackDrawable().setTint(onColor);
        } else {
            sw.getThumbDrawable().setTint(offColor);
            sw.getTrackDrawable().setTint(offColor);
        }
    }
}
