package com.example.aquasaver.ui.home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
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

    String selectedActivity = null;
    LinearLayout selectedLayout = null;

    int timerValue = 0;
    TextView timerLabel;
    PieChart pieChart;
    TextView waterUsagePreview;
    String goalType = "DAILY";
    float goal = 400f; // Default daily goal
    String units;
    String userEmail;
    boolean unDoAble = false;
    String lastLoggedDocumentId = null;

    // Firebase instances
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public HomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Get current user
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            userEmail = currentUser.getEmail();
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_LONG).show();
            return;
        }

        // Initialize views
        pieChart = view.findViewById(R.id.pieChart);
        timerLabel = view.findViewById(R.id.timerLabel);
        waterUsagePreview = view.findViewById(R.id.waterUsagePreview);
        Button incrementButton = view.findViewById(R.id.incrementTimer);
        Button decrementButton = view.findViewById(R.id.decrementTimer);
        Button logWaterUsage = view.findViewById(R.id.logWaterUsageButton);
        Button undoButton = view.findViewById(R.id.undoButton);


        ImageView shower_icon = view.findViewById(R.id.shower_icon);
        ImageView washer_icon = view.findViewById(R.id.washer_icon);
        ImageView sprinkler_icon = view.findViewById(R.id.sprinkler_icon);
        ImageView other_icon = view.findViewById(R.id.other_icon);

        // Load images from assets (only if using dynamic loading)
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

        // Get LinearLayouts for clickable areas
        LinearLayout showerButton = view.findViewById(R.id.showerButton);
        LinearLayout washerButton = view.findViewById(R.id.washerButton);
        LinearLayout sprinklerButton = view.findViewById(R.id.sprinklerButton);
        LinearLayout otherOption = view.findViewById(R.id.otherOption);

        // Set click listeners for activity selection
        showerButton.setOnClickListener(v -> setSelectedActivity(showerButton, "Shower"));
        washerButton.setOnClickListener(v -> setSelectedActivity(washerButton, "Washer"));
        sprinklerButton.setOnClickListener(v -> setSelectedActivity(sprinklerButton, "Sprinkler"));

        otherOption.setOnClickListener(v -> {
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

        // Timer controls
        incrementButton.setOnClickListener(v -> {
            timerValue++;
            updateTimerLabel();
            setWaterUsagePreview();
        });

        decrementButton.setOnClickListener(v -> {
            if (timerValue > 0) {
                timerValue--;
                updateTimerLabel();
                setWaterUsagePreview();
            }
        });

        // Load user profile and initialize chart
        loadUserProfileAndUpdateChart();

        // Log water usage
        logWaterUsage.setOnClickListener(v -> {
            if (selectedActivity == null || timerValue == 0) {
                Toast.makeText(requireContext(), "Please select an activity and set the time.", Toast.LENGTH_SHORT).show();
                return;
            }

            logWaterUsage();
        });

        // Undo button
        undoButton.setOnClickListener(v -> undoLastLog());
    }

    private void loadUserProfileAndUpdateChart() {
        db.collection("users").document(userEmail)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
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
                    }

                    // Load water usage and update chart
                    loadWaterUsageAndUpdateChart();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error loading user profile", Toast.LENGTH_SHORT).show();
                    loadWaterUsageAndUpdateChart(); // Use defaults
                });
    }

    private void loadWaterUsageAndUpdateChart() {
        long[] window = getTimeWindow();

        db.collection("waterUsage")
                .whereEqualTo("userEmail", userEmail)
                .whereGreaterThanOrEqualTo("timestamp", new Date(window[0]))
                .whereLessThan("timestamp", new Date(window[1]))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    float totalUsage = 0f;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Double amount = doc.getDouble("amountLiters");
                        if (amount != null) {
                            totalUsage += amount.floatValue();
                        }
                    }
                    updatePieChart(totalUsage, goal, units);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error loading water usage", Toast.LENGTH_SHORT).show();
                    updatePieChart(0f, goal, units);
                });
    }

    private void logWaterUsage() {
        double multiplier = getWaterUsageMultiplier(selectedActivity);
        double litersUsed = timerValue * multiplier;

        Map<String, Object> waterUsage = new HashMap<>();
        waterUsage.put("userEmail", userEmail);
        waterUsage.put("timestamp", new Date());
        waterUsage.put("amountLiters", litersUsed);
        waterUsage.put("activityType", selectedActivity);

        db.collection("waterUsage")
                .add(waterUsage)
                .addOnSuccessListener(documentReference -> {
                    lastLoggedDocumentId = documentReference.getId();
                    unDoAble = true;

                    // Reset UI
                    timerValue = 0;
                    updateTimerLabel();

                    if (selectedLayout != null) {
                        selectedLayout.setBackgroundResource(R.drawable.default_background);
                        selectedLayout = null;
                    }
                    selectedActivity = null;
                    setWaterUsagePreview();

                    // Reload chart
                    loadWaterUsageAndUpdateChart();

                    Toast.makeText(requireContext(), "Water usage logged", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error logging water usage", Toast.LENGTH_SHORT).show();
                });
    }

    private void undoLastLog() {
        if (!unDoAble || lastLoggedDocumentId == null) {
            Toast.makeText(requireContext(), "Please log your water usage to undo", Toast.LENGTH_SHORT).show();
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
                                    unDoAble = false;
                                    lastLoggedDocumentId = null;

                                    // Reload chart
                                    loadWaterUsageAndUpdateChart();

                                    String message = "Undid log: " + activityType + " (" +
                                            String.format("%.1f", amountLiters) + " L)";
                                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(requireContext(), "Error undoing log", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
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
        timerLabel.setText("Timer: " + timerValue + " min");
    }

    private void updatePieChart(float usage, float goal, String units) {
        ArrayList<PieEntry> entries = new ArrayList<>();

        float usageLiters = usage;
        float totalLiters = goal;
        float remaining = Math.max(totalLiters - usageLiters, 0);

        entries.add(new PieEntry(usageLiters, "Used"));
        entries.add(new PieEntry(remaining, "Remaining"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(
                ContextCompat.getColor(requireContext(), R.color.light_blue),
                ContextCompat.getColor(requireContext(), R.color.dark_blue)
        );
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (units.equals("Liters"))
                {
                    return String.format("%.1f L", value);
                }
                return String.format("%.1f G", value);

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
                "%.1f / %.1f liters\n%.1f%% %s",
                usageLiters,
                totalLiters,
                percentUsed,
                label
        );

        pieChart.setCenterText(centerText);
        pieChart.setCenterTextSize(16f);
        pieChart.setCenterTextColor(Color.BLACK);
        pieChart.getDescription().setEnabled(false);
        pieChart.invalidate();
    }

    private void setSelectedActivity(LinearLayout layout, String activity) {
        if (selectedLayout != null) {
            selectedLayout.setBackgroundResource(R.drawable.default_background);
        }
        layout.setBackgroundResource(R.drawable.selected_background);
        selectedLayout = layout;
        selectedActivity = activity;
        setWaterUsagePreview();
    }

    private void setWaterUsagePreview() {
        if (selectedActivity != null && timerValue > 0) {
            double multiplier = getWaterUsageMultiplier(selectedActivity);
            double litersUsed = timerValue * multiplier;
            waterUsagePreview.setText(
                    String.format("Estimated Usage: %.1f liters for %d min (%s)",
                            litersUsed, timerValue, selectedActivity)
            );
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
}