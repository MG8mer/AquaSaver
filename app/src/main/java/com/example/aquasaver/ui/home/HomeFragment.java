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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.aquasaver.R;
import com.example.aquasaver.repository.GoalProgressRepository;
import com.example.aquasaver.repository.UserProfileRepository;
import com.example.aquasaver.repository.WaterUsageRepository;
import com.example.aquasaver.db.AppDatabase;
import com.example.aquasaver.model.GoalProgress;
import com.example.aquasaver.model.UserProfile;
import com.example.aquasaver.model.WaterUsage;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {

    String selectedActivity = null;
    LinearLayout selectedLayout = null;

    int timerValue = 0;
    TextView timerLabel;
    PieChart pieChart;
    AppDatabase db;
    WaterUsageRepository waterUsageDao;
    UserProfileRepository userProfileDao;
    GoalProgressRepository goalProgressDao;
    TextView waterUsagePreview;
    UserProfile user;
    String goalType;
    float goal;

    boolean unDoAble = false;

    public HomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pieChart = view.findViewById(R.id.pieChart);
        timerLabel = view.findViewById(R.id.timerLabel);
        waterUsagePreview = view.findViewById(R.id.waterUsagePreview);
        Button incrementButton = view.findViewById(R.id.incrementTimer);
        Button decrementButton = view.findViewById(R.id.decrementTimer);
        Button logWaterUsage = view.findViewById(R.id.logWaterUsageButton);
        Button undoButton = view.findViewById(R.id.undoButton);

        LinearLayout showerButton = view.findViewById(R.id.showerButton);
        LinearLayout washerButton = view.findViewById(R.id.washerButton);
        LinearLayout sprinklerButton = view.findViewById(R.id.sprinklerButton);
        LinearLayout otherOption = view.findViewById(R.id.otherOption);

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


        new Thread(() -> {
            db = AppDatabase.getInstance(requireContext());
            waterUsageDao = db.waterUsageDao();
            userProfileDao = db.userProfileDao();
            goalProgressDao = db.goalProgressDao();

            SharedPreferences prefs = requireActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
            String userEmail = prefs.getString("username", null);

            if (userEmail == null) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_LONG).show());
                return;
            }

            user = userProfileDao.getUserByEmail(userEmail);
            if (user == null) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "User not found. Please log in.", Toast.LENGTH_LONG).show());
                return;
            }

            goalType = user.getGoalType() != null ? user.getGoalType().toString() : "DAILY";

            float totalUsage;
            if ("DAILY".equals(goalType)) {
                long[] window = computeTodayWindow();
                totalUsage = waterUsageDao.getLitersUsedBetween(userEmail, window[0], window[1]);
            } else if ("WEEKLY".equals(goalType)) {
                long[] window = computeCurrentWeekWindow();
                totalUsage = waterUsageDao.getLitersUsedBetween(userEmail, window[0], window[1]);
            } else {
                long[] window = computeCurrentMonthWindow();
                totalUsage = waterUsageDao.getLitersUsedBetween(userEmail, window[0], window[1]);
            }

            List<GoalProgress> goalProgressList = goalProgressDao.getAllProgressForUser(userEmail);
            GoalProgress defaultGoal;
            if (goalProgressList.isEmpty()) {
                if ("DAILY".equals(goalType)) {
                    defaultGoal = new GoalProgress(userEmail, 0f, new Date(), true, 400);
                } else if ("WEEKLY".equals(goalType)) {
                    defaultGoal = new GoalProgress(userEmail, 0f, new Date(), true, 2800);
                } else {
                   defaultGoal = new GoalProgress(userEmail, 0f, new Date(), true, 11200);
                }
                goalProgressDao.insertGoalProgress(defaultGoal);
                goalProgressList = goalProgressDao.getAllProgressForUser(userEmail);
            }

            goal = goalProgressList.get(0).getGoalAmount();

            List<GoalProgress> finalGoalProgressList = goalProgressList;
            float finalTotalUsage = totalUsage;

            requireActivity().runOnUiThread(() -> {
                updatePieChart(finalTotalUsage, goal);

                logWaterUsage.setOnClickListener(v -> {
                    if (selectedActivity == null || timerValue == 0) {
                        Toast.makeText(requireContext(), "Please select an activity and set the time.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double multiplier;
                    switch (selectedActivity) {
                        case "Shower":
                            multiplier = 9;  // liters per minute (mid-range shower flow)
                            break;
                        case "Sprinkler":
                            multiplier = 9;  // liters per minute (estimate)
                            break;
                        case "Washer":
                            multiplier = 2;  // liters per minute (modern efficient washer)
                            break;
                        case "Washing Car":
                        case "Cleaning":
                        case "Filling Pool":
                            multiplier = 10; // liters per minute (estimate)
                            break;
                        case "Watering Garden":
                            multiplier = 5;  // liters per minute
                            break;
                        default:
                            multiplier = 1;  // fallback liters per unit time
                    }

                    double litersUsed = timerValue * multiplier;
                    Date currentDate = new Date();
                    WaterUsage usage = new WaterUsage(userEmail, currentDate, litersUsed, selectedActivity);

                    new Thread(() -> {
                        if (userProfileDao.getUserByEmail(userEmail) == null) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "User does not exist: " + userEmail, Toast.LENGTH_LONG).show());
                            return;
                        }

                        waterUsageDao.insertLog(usage);
                        unDoAble = true;

                        float updatedUsage;
                        if ("DAILY".equals(goalType)) {
                            long[] window = computeTodayWindow();
                            updatedUsage = waterUsageDao.getLitersUsedBetween(userEmail, window[0], window[1]);
                        } else if ("WEEKLY".equals(goalType)) {
                            long[] window = computeCurrentWeekWindow();
                            updatedUsage = waterUsageDao.getLitersUsedBetween(userEmail, window[0], window[1]);
                        } else {
                            long[] window = computeCurrentMonthWindow();
                            updatedUsage = waterUsageDao.getLitersUsedBetween(userEmail, window[0], window[1]);
                        }

                        GoalProgress mostRecentGoal = finalGoalProgressList.get(0);
                        mostRecentGoal.setAmountLogged(updatedUsage);
                        mostRecentGoal.setProgressDate(new Date());
                        mostRecentGoal.setOnTarget(updatedUsage <= goal);
                        goalProgressDao.updateGoalProgress(mostRecentGoal);

                        requireActivity().runOnUiThread(() -> {
                            timerValue = 0;
                            updateTimerLabel();
                            updatePieChart(updatedUsage, goal);
                            Toast.makeText(requireContext(), "Water usage logged", Toast.LENGTH_SHORT).show();

                            if (selectedLayout != null) {
                                selectedLayout.setBackgroundResource(R.drawable.default_background);
                                selectedLayout = null;
                            }
                            selectedActivity = null;
                        });
                    }).start();
                });
            });
        }).start();

        undoButton.setOnClickListener(v -> {
            new Thread(() -> {
                WaterUsage latestLog = waterUsageDao.getLatestLogForUser(user.getEmail());

                if (latestLog != null && unDoAble) {
                    waterUsageDao.deleteLog(latestLog);
                    unDoAble = false;

                    float updatedUsage;
                    if ("DAILY".equals(goalType)) {
                        long[] window = computeTodayWindow();
                        updatedUsage = waterUsageDao.getLitersUsedBetween(user.getEmail(), window[0], window[1]);
                    } else if ("WEEKLY".equals(goalType)) {
                        long[] window = computeCurrentWeekWindow();
                        updatedUsage = waterUsageDao.getLitersUsedBetween(user.getEmail(), window[0], window[1]);
                    } else {
                        long[] window = computeCurrentMonthWindow();
                        updatedUsage = waterUsageDao.getLitersUsedBetween(user.getEmail(), window[0], window[1]);
                    }

                    requireActivity().runOnUiThread(() -> {
                        updatePieChart(updatedUsage, goal);
                        Toast.makeText(requireContext(),
                                "Undid log: " + latestLog.getActivityType() +
                                        " (" + latestLog.getAmountLiters() + " L)", Toast.LENGTH_SHORT).show();
                    });

                } else {
                    // No log to undo
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(),
                                    "Please log your water usage to undo", Toast.LENGTH_SHORT).show());
                }
            }).start();
        });
    }

    private void updateTimerLabel() {
        timerLabel.setText("Timer: " + timerValue + " min");
    }

    private void updatePieChart(float usage, float goal) {
        ArrayList<PieEntry> entries = new ArrayList<>();

        // usage and goal already in liters
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
                return String.format("%.1f L", value);
            }
        });

        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        float percentUsed = totalLiters != 0 ? (usageLiters / totalLiters) * 100f : 0f;

        String label;
        switch (goalType) {
            case "DAILY":
                label = "used today";
                break;
            case "WEEKLY":
                label = "used this week";
                break;
            default:
                label = "used this month";
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
        if (selectedActivity != null && timerValue > 0)
        {
            double multiplier;
            switch (selectedActivity) {
                case "Shower":
                    multiplier = 9;  // liters per minute (mid-range shower flow)
                    break;
                case "Sprinkler":
                    multiplier = 9;  // liters per minute (estimate)
                    break;
                case "Washer":
                    multiplier = 2;  // liters per minute (modern efficient washer)
                    break;
                case "Washing Car":
                case "Cleaning":
                case "Filling Pool":
                    multiplier = 10; // liters per minute (estimate)
                    break;
                case "Watering Garden":
                    multiplier = 5;  // liters per minute
                    break;
                default:
                    multiplier = 1;  // fallback liters per unit time
            }

            double litersUsed = timerValue * multiplier;
            waterUsagePreview.setText(
                    String.format("Estimated Usage: %.1f liters for %d min (%s)",
                            litersUsed, timerValue, selectedActivity)
            );
        }
        else {
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
