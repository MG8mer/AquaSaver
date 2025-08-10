package com.example.aquasaver.ui.home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.aquasaver.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    String selectedActivity = null;
    LinearLayout selectedLayout = null;

    int timerValue = 0;
    TextView timerLabel;
    PieChart pieChart;
    TextView waterUsagePreview;
    String goalType = "DAILY";
    float goal = 400f; // Default daily goal
    String units = "Liters"; // Initialize units with default value
    String userEmail;
    boolean unDoAble = false;
    String lastLoggedDocumentId = null;

    // Firebase instances
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;

    public HomeFragment() {}

    @Override
    public void onStart() {
        super.onStart();
        // Re-check authentication when fragment starts
        if (auth != null && authStateListener != null) {
            auth.addAuthStateListener(authStateListener);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // Remove auth listener to prevent memory leaks
        if (auth != null && authStateListener != null) {
            auth.removeAuthStateListener(authStateListener);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "onViewCreated called");
        debugAuthState();

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Wait for authentication state to be ready
        authStateListener = firebaseAuth -> {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null) {
                userEmail = currentUser.getEmail();
                Log.d(TAG, "User authenticated: " + userEmail);

                // Initialize UI only after user is confirmed
                if (userEmail != null && !userEmail.isEmpty()) {
                    initializeUI(view);
                } else {
                    Log.e(TAG, "User email is null or empty");
                    Toast.makeText(requireContext(), "User email not available", Toast.LENGTH_LONG).show();
                }
            } else {
                Log.e(TAG, "No authenticated user found");
                Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_LONG).show();
                // Optionally redirect to login screen here
            }
        };

        auth.addAuthStateListener(authStateListener);

        // Also try immediate check in case user is already available
        FirebaseUser immediateUser = auth.getCurrentUser();
        if (immediateUser != null && immediateUser.getEmail() != null) {
            userEmail = immediateUser.getEmail();
            Log.d(TAG, "Immediate user check successful: " + userEmail);
            initializeUI(view);
        }
    }

    private void initializeUI(@NonNull View view) {
        Log.d(TAG, "Initializing UI for user: " + userEmail);

        // Initialize views
        pieChart = view.findViewById(R.id.pieChart);
        timerLabel = view.findViewById(R.id.timerLabel);
        waterUsagePreview = view.findViewById(R.id.waterUsagePreview);

        Button incrementButton = view.findViewById(R.id.incrementTimer);
        Button decrementButton = view.findViewById(R.id.decrementTimer);
        Button logWaterUsage = view.findViewById(R.id.logWaterUsageButton);
        Button undoButton = view.findViewById(R.id.undoButton);

        // Check if views are found
        if (incrementButton == null) Log.e(TAG, "incrementTimer button not found");
        if (decrementButton == null) Log.e(TAG, "decrementTimer button not found");
        if (logWaterUsage == null) Log.e(TAG, "logWaterUsageButton not found");

        ImageView shower_icon = view.findViewById(R.id.shower_icon);
        ImageView washer_icon = view.findViewById(R.id.washer_icon);
        ImageView sprinkler_icon = view.findViewById(R.id.sprinkler_icon);
        ImageView other_icon = view.findViewById(R.id.other_icon);

        // Load images from drawable resources instead of assets
        // Remove Glide loading if you're using drawable resources
        /*
        Glide.with(this)
                .load("file:///android_asset/shower-icon.png")
                .into(shower_icon);

        Glide.with(this)
                .load("file:///android_asset/washer-icon.png")
                .into(washer_icon);

        Glide.with(this)
                .load("file:///android_asset/sprinkler-icon.png")
                .into(sprinkler_icon);

        Glide.with(this)
                .load("file:///android_asset/other-icon.png")
                .into(other_icon);
        */

        // Get LinearLayouts for clickable areas
        LinearLayout showerButton = view.findViewById(R.id.showerButton);
        LinearLayout washerButton = view.findViewById(R.id.washerButton);
        LinearLayout sprinklerButton = view.findViewById(R.id.sprinklerButton);
        LinearLayout otherOption = view.findViewById(R.id.otherOption);

        // Check if activity buttons are found
        if (showerButton == null) Log.e(TAG, "showerButton not found");
        if (washerButton == null) Log.e(TAG, "washerButton not found");
        if (sprinklerButton == null) Log.e(TAG, "sprinklerButton not found");
        if (otherOption == null) Log.e(TAG, "otherOption not found");

        // Set click listeners for activity selection
        if (showerButton != null) {
            showerButton.setOnClickListener(v -> {
                Log.d(TAG, "Shower button clicked");
                setSelectedActivity(showerButton, "Shower");
            });
        }

        if (washerButton != null) {
            washerButton.setOnClickListener(v -> {
                Log.d(TAG, "Washer button clicked");
                setSelectedActivity(washerButton, "Washer");
            });
        }

        if (sprinklerButton != null) {
            sprinklerButton.setOnClickListener(v -> {
                Log.d(TAG, "Sprinkler button clicked");
                setSelectedActivity(sprinklerButton, "Sprinkler");
            });
        }

        if (otherOption != null) {
            otherOption.setOnClickListener(v -> {
                Log.d(TAG, "Other option clicked");
                String[] otherActivities = {"Washing Car", "Watering Garden", "Cleaning", "Filling Pool"};
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Select Activity");
                builder.setItems(otherActivities, (dialog, which) -> {
                    setSelectedActivity(otherOption, otherActivities[which]);
                    Toast.makeText(requireContext(), "Selected: " + otherActivities[which], Toast.LENGTH_SHORT).show();
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();
            });
        }

        // Timer controls
        if (incrementButton != null) {
            incrementButton.setOnClickListener(v -> {
                Log.d(TAG, "Increment button clicked");
                timerValue++;
                updateTimerLabel();
                setWaterUsagePreview();
            });
        }

        if (decrementButton != null) {
            decrementButton.setOnClickListener(v -> {
                Log.d(TAG, "Decrement button clicked");
                if (timerValue > 0) {
                    timerValue--;
                    updateTimerLabel();
                    setWaterUsagePreview();
                }
            });
        }

        // Initialize timer label
        updateTimerLabel();
        setWaterUsagePreview();

        // Load user profile and initialize chart
        loadUserProfileAndUpdateChart();

        // Log water usage
        if (logWaterUsage != null) {
            logWaterUsage.setOnClickListener(v -> {
                Log.d(TAG, "Log water usage button clicked");
                if (selectedActivity == null || timerValue == 0) {
                    Toast.makeText(requireContext(), "Please select an activity and set the time.", Toast.LENGTH_SHORT).show();
                    return;
                }
                logWaterUsage();
            });
        }

        // Undo button
        if (undoButton != null) {
            undoButton.setOnClickListener(v -> {
                Log.d(TAG, "Undo button clicked");
                undoLastLog();
            });
        }

        // Set up conservation tips buttons
        Button conservationTipsButton = view.findViewById(R.id.conservationTipsButton);
        Button ecoChallengesButton = view.findViewById(R.id.ecoChallengesButton);

        if (conservationTipsButton != null) {
            conservationTipsButton.setOnClickListener(v -> {
                // Navigate to conservation tips
                Toast.makeText(requireContext(), "Conservation Tips clicked", Toast.LENGTH_SHORT).show();
                // Add navigation logic here
            });
        }

        if (ecoChallengesButton != null) {
            ecoChallengesButton.setOnClickListener(v -> {
                // Navigate to eco challenges
                Toast.makeText(requireContext(), "Eco Challenges clicked", Toast.LENGTH_SHORT).show();
                // Add navigation logic here
            });
        }
    }

    private void loadUserProfileAndUpdateChart() {
        Log.d(TAG, "Loading user profile for: " + userEmail);

        db.collection("users").document(userEmail)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Log.d(TAG, "User profile loaded successfully");

                    if (documentSnapshot.exists()) {
                        goalType = documentSnapshot.getString("goalType");
                        if (goalType == null) goalType = "DAILY";

                        // Set default goals based on goal type
                        switch (goalType) {
                            case "WEEKLY":
                                goal = 2800f;
                                break;
                            case "MONTHLY":
                                goal = 11200f;
                                break;
                            default:
                                goal = 400f;
                        }

                        // Get custom goal if exists
                        Double customGoal = documentSnapshot.getDouble("goalAmount");
                        String goalUnits = documentSnapshot.getString("goalUnits");
                        if (customGoal != null) {
                            goal = customGoal.floatValue();
                        }

                        // Initialize units properly
                        units = goalUnits != null ? goalUnits : "Liters";

                        Log.d(TAG, "Goal type: " + goalType + ", Goal: " + goal + ", Units: " + units);
                    } else {
                        // Set default values if user profile doesn't exist
                        units = "Liters";
                        Log.d(TAG, "User profile doesn't exist, using defaults");
                    }

                    // Load water usage and update chart
                    loadWaterUsageAndUpdateChart();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading user profile", e);
                    Toast.makeText(requireContext(), "Error loading user profile", Toast.LENGTH_SHORT).show();
                    units = "Liters"; // Set default units
                    loadWaterUsageAndUpdateChart(); // Use defaults
                });
    }

    private void loadWaterUsageAndUpdateChart() {
        Log.d(TAG, "Loading water usage data");

        long[] window = getTimeWindow();
        Log.d(TAG, "Time window: " + new Date(window[0]) + " to " + new Date(window[1]));

        db.collection("waterUsage")
                .whereEqualTo("userEmail", userEmail)
                .whereGreaterThanOrEqualTo("timestamp", new Date(window[0]))
                .whereLessThan("timestamp", new Date(window[1]))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Water usage data loaded successfully. Documents: " + queryDocumentSnapshots.size());

                    float totalUsage = 0f;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Double amount = doc.getDouble("amountLiters");
                        if (amount != null) {
                            totalUsage += amount.floatValue();
                            Log.d(TAG, "Added usage: " + amount.floatValue());
                        }
                    }

                    Log.d(TAG, "Total usage: " + totalUsage + ", Goal: " + goal + ", Units: " + units);
                    updatePieChart(totalUsage, goal, units);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading water usage", e);
                    Toast.makeText(requireContext(), "Error loading water usage", Toast.LENGTH_SHORT).show();
                    updatePieChart(0f, goal, units);
                });
    }

    private void logWaterUsage() {
        Log.d(TAG, "Logging water usage for activity: " + selectedActivity + ", time: " + timerValue);

        double multiplier = getWaterUsageMultiplier(selectedActivity);
        double litersUsed = timerValue * multiplier;

        Log.d(TAG, "Multiplier: " + multiplier + ", Liters used: " + litersUsed);

        Map<String, Object> waterUsage = new HashMap<>();
        waterUsage.put("userEmail", userEmail);
        waterUsage.put("timestamp", new Date());
        waterUsage.put("amountLiters", litersUsed);
        waterUsage.put("activityType", selectedActivity);

        db.collection("waterUsage")
                .add(waterUsage)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Water usage logged successfully with ID: " + documentReference.getId());

                    lastLoggedDocumentId = documentReference.getId();
                    unDoAble = true;

                    // Reset UI
                    timerValue = 0;
                    updateTimerLabel();

                    if (selectedLayout != null) {
                        selectedLayout.setBackgroundColor(Color.TRANSPARENT);
                        selectedLayout = null;
                    }
                    selectedActivity = null;
                    setWaterUsagePreview();

                    // Reload chart
                    loadWaterUsageAndUpdateChart();

                    Toast.makeText(requireContext(), "Water usage logged: " + String.format("%.1f", litersUsed) + " L", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error logging water usage", e);
                    Toast.makeText(requireContext(), "Error logging water usage", Toast.LENGTH_SHORT).show();
                });
    }

    private void undoLastLog() {
        Log.d(TAG, "Attempting to undo last log");

        if (!unDoAble || lastLoggedDocumentId == null) {
            Toast.makeText(requireContext(), "No recent log to undo", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("waterUsage").document(lastLoggedDocumentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String activityType = documentSnapshot.getString("activityType");
                        Double amountLiters = documentSnapshot.getDouble("amountLiters");

                        // Delete the document
                        db.collection("waterUsage").document(lastLoggedDocumentId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Successfully undid log for: " + activityType);

                                    unDoAble = false;
                                    lastLoggedDocumentId = null;

                                    // Reload chart
                                    loadWaterUsageAndUpdateChart();

                                    String message = "Undid log: " + activityType + " (" +
                                            String.format("%.1f", amountLiters) + " L)";
                                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error undoing log", e);
                                    Toast.makeText(requireContext(), "Error undoing log", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Log.w(TAG, "Document to undo not found");
                        Toast.makeText(requireContext(), "Log not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error finding log to undo", e);
                    Toast.makeText(requireContext(), "Error finding log to undo", Toast.LENGTH_SHORT).show();
                });
    }

    private double getWaterUsageMultiplier(String activity) {
        switch (activity) {
            case "Shower":
                return 9.0;  // liters per minute
            case "Sprinkler":
                return 9.0;  // liters per minute
            case "Washer":
                return 2.0;  // liters per minute
            case "Washing Car":
            case "Cleaning":
            case "Filling Pool":
                return 10.0; // liters per minute
            case "Watering Garden":
                return 5.0;  // liters per minute
            default:
                return 1.0;  // fallback
        }
    }

    private long[] getTimeWindow() {
        switch (goalType) {
            case "WEEKLY":
                return computeCurrentWeekWindow();
            case "MONTHLY":
                return computeCurrentMonthWindow();
            default:
                return computeTodayWindow();
        }
    }

    private void updateTimerLabel() {
        if (timerLabel != null) {
            timerLabel.setText("Timer: " + timerValue + " min");
            Log.d(TAG, "Timer updated to: " + timerValue);
        }
    }

    private void updatePieChart(float usage, float goal, String units) {
        if (pieChart == null) {
            Log.e(TAG, "PieChart is null, cannot update");
            return;
        }

        Log.d(TAG, "Updating pie chart - Usage: " + usage + ", Goal: " + goal + ", Units: " + units);

        ArrayList<PieEntry> entries = new ArrayList<>();

        float usageLiters = usage;
        float totalLiters = goal;
        float remaining = Math.max(totalLiters - usageLiters, 0);

        entries.add(new PieEntry(usageLiters, "Used"));
        entries.add(new PieEntry(remaining, "Remaining"));

        PieDataSet dataSet = new PieDataSet(entries, "");

        // Use default colors if custom colors are not available
        try {
            dataSet.setColors(
                    ContextCompat.getColor(requireContext(), R.color.light_blue),
                    ContextCompat.getColor(requireContext(), R.color.dark_blue)
            );
        } catch (Exception e) {
            // Fallback to default colors
            dataSet.setColors(Color.BLUE, Color.CYAN);
            Log.w(TAG, "Using default colors for pie chart", e);
        }

        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (units != null && units.equals("Gallons")) {
                    return String.format("%.1f G", value);
                }
                return String.format("%.1f L", value);
            }
        });

        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        float percentUsed = totalLiters != 0 ? (usageLiters / totalLiters) * 100f : 0f;

        String label;
        switch (goalType) {
            case "WEEKLY":
                label = "used this week";
                break;
            case "MONTHLY":
                label = "used this month";
                break;
            default:
                label = "used today";
                break;
        }

        String centerText = String.format(
                "%.1f / %.1f %s\n%.1f%% %s",
                usageLiters,
                totalLiters,
                units != null ? (units.equals("Gallons") ? "gal" : "L") : "L",
                percentUsed,
                label
        );

        pieChart.setCenterText(centerText);
        pieChart.setCenterTextSize(14f);
        pieChart.setCenterTextColor(Color.BLACK);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.invalidate();

        Log.d(TAG, "Pie chart updated successfully");
    }

    private void setSelectedActivity(LinearLayout layout, String activity) {
        Log.d(TAG, "Setting selected activity: " + activity);

        // Clear previous selection
        if (selectedLayout != null) {
            selectedLayout.setBackgroundColor(Color.TRANSPARENT);
        }

        // Set new selection
        try {
            layout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_blue));
        } catch (Exception e) {
            // Fallback color
            layout.setBackgroundColor(Color.LTGRAY);
            Log.w(TAG, "Using fallback selection color", e);
        }

        selectedLayout = layout;
        selectedActivity = activity;
        setWaterUsagePreview();

        Log.d(TAG, "Activity selected: " + selectedActivity);
    }

    private void setWaterUsagePreview() {
        if (waterUsagePreview == null) return;

        if (selectedActivity != null && timerValue > 0) {
            double multiplier = getWaterUsageMultiplier(selectedActivity);
            double litersUsed = timerValue * multiplier;
            String previewText = String.format("Estimated Usage: %.1f liters for %d min (%s)",
                    litersUsed, timerValue, selectedActivity);
            waterUsagePreview.setText(previewText);
            Log.d(TAG, "Preview updated: " + previewText);
        } else {
            waterUsagePreview.setText("Select an activity and set a timer");
        }
    }

    public static long[] computeTodayWindow() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long start = cal.getTimeInMillis();

        cal.add(Calendar.DAY_OF_YEAR, 1);
        long end = cal.getTimeInMillis();

        return new long[]{start, end};
    }

    public static long[] computeCurrentWeekWindow() {
        Calendar now = Calendar.getInstance();
        int daysSinceMonday = (now.get(Calendar.DAY_OF_WEEK) + 5) % 7;
        now.add(Calendar.DAY_OF_YEAR, -daysSinceMonday);
        now.set(Calendar.HOUR_OF_DAY, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);
        long start = now.getTimeInMillis();

        now.add(Calendar.DAY_OF_YEAR, 7);
        long end = now.getTimeInMillis();

        return new long[]{start, end};
    }

    public static long[] computeCurrentMonthWindow() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long start = cal.getTimeInMillis();

        cal.add(Calendar.MONTH, 1);
        long end = cal.getTimeInMillis();

        return new long[]{start, end};
    }

    private void debugAuthState() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        Log.d(TAG, "=== AUTH DEBUG ===");
        Log.d(TAG, "Auth instance: " + auth);
        Log.d(TAG, "Current user: " + user);

        if (user != null) {
            Log.d(TAG, "User UID: " + user.getUid());
            Log.d(TAG, "User email: " + user.getEmail());
            Log.d(TAG, "Is email verified: " + user.isEmailVerified());
        } else {
            Log.d(TAG, "User is NULL");
        }
        Log.d(TAG, "================");
    }
}