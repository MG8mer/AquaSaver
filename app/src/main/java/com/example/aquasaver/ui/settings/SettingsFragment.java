package com.example.aquasaver.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
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

import com.example.aquasaver.R;
import com.example.aquasaver.model.GoalProgress;
import com.example.aquasaver.model.UserProfile;
import com.example.aquasaver.model.enums.GoalType;
import com.example.aquasaver.repository.GoalProgressRepository;
import com.example.aquasaver.repository.UserProfileRepository;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment {

    private UserProfileRepository userProfileRepo;
    private GoalProgressRepository goalProgressRepo;

    private TextView textUserEmail;
    private SwitchCompat switchNotifications, switchGPS, switchWeatherAlerts;
    private Button buttonSave;

    private EditText editCurrentPassword, editNewPassword, editLocation, goalAmount;
    private Spinner spinnerGoalType, spinnerUnitType;

    final UserProfile[] userProfile = new UserProfile[1];

    List<GoalProgress>[] goalProgress = new List[]{new ArrayList<>()};

    private String userEmail;  // will hold the logged-in user email

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize views
        textUserEmail = view.findViewById(R.id.text_user_email);
        buttonSave = view.findViewById(R.id.button_save_settings);

        editCurrentPassword = view.findViewById(R.id.edit_current_password);
        editNewPassword = view.findViewById(R.id.edit_new_password);
        editNewPassword.setEnabled(false); // Disabled until current password is verified
        spinnerGoalType = view.findViewById(R.id.spinner_goal_type);
        spinnerUnitType = view.findViewById(R.id.spinner_units);
        goalAmount = view.findViewById(R.id.edit_custom_goal);
        String input = goalAmount.toString();
        int goal = 1;
        try {
            goal = Integer.parseInt(input);
            if (goal <= 0) {
                Toast.makeText(getContext(), "Goal must be greater than 0", Toast.LENGTH_SHORT).show();
            }

            // Use the validated goal here
            Log.d("GoalValidation", "Valid goal: " + goal);

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid number format", Toast.LENGTH_SHORT).show();
        }

        // Setup spinner adapters
        ArrayAdapter<GoalType> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, GoalType.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGoalType.setAdapter(adapter);

        userProfileRepo = new UserProfileRepository();
        goalProgressRepo = new GoalProgressRepository();

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

        // Listen to current password input changes to verify and enable new password
        editCurrentPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (userProfile[0] == null) {
                    editNewPassword.setEnabled(false);
                    return;
                }
                String input = s.toString().trim();
                if (input.equals(userProfile[0].getPasswordHash())) {
                    editNewPassword.setEnabled(true);
                } else {
                    editNewPassword.setEnabled(false);
                    editNewPassword.setText("");
                }
            }
            @Override public void afterTextChanged(Editable s) { }
        });

        int finalGoal = goal;
        buttonSave.setOnClickListener(v -> {
            if (userProfile[0] == null) {
                Toast.makeText(requireContext(), "User profile not loaded", Toast.LENGTH_SHORT).show();
                return;
            }

            String currentPwdInput = editCurrentPassword.getText().toString().trim();
            boolean isCurrentPwdCorrect = !TextUtils.isEmpty(currentPwdInput) && currentPwdInput.equals(userProfile[0].getPasswordHash());

            // If new password is entered, current password must be correct
            if (!TextUtils.isEmpty(editNewPassword.getText().toString().trim()) && !isCurrentPwdCorrect) {
                Toast.makeText(requireContext(), "Current password incorrect", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isCurrentPwdCorrect && !TextUtils.isEmpty(editNewPassword.getText().toString().trim())) {
                userProfile[0].setPasswordHash(editNewPassword.getText().toString().trim());
            }

            String locationInput = editLocation.getText().toString().trim();
            if (!locationInput.isEmpty()) {
                userProfile[0].setLocation(locationInput);
            }

            GoalType selectedGoal = (GoalType) spinnerGoalType.getSelectedItem();
            userProfile[0].setGoalType(selectedGoal);

            goalProgress[0].get(0).setGoalAmount(finalGoal);
            goalProgress[0].get(0).setGoalUnits(spinnerUnitType.getSelectedItem().toString());

            // Update DB in background thread
            new Thread(() -> {
                userProfileRepo.updateUserProfile(userProfile[0], count ->
                {
                    Toast.makeText(requireContext(), "Settings updated", Toast.LENGTH_SHORT).show();
                }, e -> {
                    Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show();
                });
            }).start();
        });

        return view;
    }

    private void loadUserProfile(String email) {
        new Thread(() -> {
            userProfileRepo.getUserProfilesByEmail(email, snapshot -> {
                if (!snapshot.isEmpty())
                {
                    userProfile[0] = snapshot.getDocuments().get(0).toObject(UserProfile.class);
                }
            });

            goalProgressRepo.getAllProgressForUser(email, snapshot -> {
                for (DocumentSnapshot doc : snapshot) {
                    GoalProgress gP = doc.toObject(GoalProgress.class);
                    if (gP != null) goalProgress[0].add(gP);
                }
            });
            if (userProfile != null) {
                requireActivity().runOnUiThread(() -> {
                    // Display user email
                    textUserEmail.setText(userProfile[0].getEmail());

                    goalAmount.setText(goalProgress[0].get(0).getGoalAmount());


                    GoalType[] goals = GoalType.values();
                    for (int i = 0; i < goals.length; i++) {
                        if (goals[i] == userProfile[0].getGoalType()) {
                            spinnerGoalType.setSelection(i);
                            break;
                        }
                    }
                    String[] units = {"Liters", "Gallons"};
                    for (int i = 0; i < units.length; i++) {
                        if (units[i].equals(goalProgress[0].get(0).getGoalUnits())) {
                            spinnerUnitType.setSelection(i);
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
