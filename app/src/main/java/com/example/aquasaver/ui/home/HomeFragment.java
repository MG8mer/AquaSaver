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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.aquasaver.R;
import com.example.aquasaver.dao.GoalProgressDao;
import com.example.aquasaver.dao.UserProfileDao;
import com.example.aquasaver.dao.WaterUsageDao;
import com.example.aquasaver.db.AppDatabase;
import com.example.aquasaver.model.GoalProgress;
import com.example.aquasaver.model.UserProfile;
import com.example.aquasaver.model.WaterUsage;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;

public class HomeFragment extends Fragment {

    // to store selected activity on the home page
    String selectedActivity = null;
    LinearLayout selectedLayout = null;


    int timerValue = 0;
    TextView timerLabel;
    PieChart pieChart;
    AppDatabase db;
    WaterUsageDao waterUsageDao;
    UserProfileDao userProfileDao;

    GoalProgressDao goalProgressDao;
    UserProfile user;

    String goalType;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate your fragment layout (rename your XML accordingly)
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pieChart = view.findViewById(R.id.pieChart);
        timerLabel = view.findViewById(R.id.timerLabel);
        Button incrementButton = view.findViewById(R.id.incrementTimer);
        Button decrementButton = view.findViewById(R.id.decrementTimer);
        Button logWaterUsage = view.findViewById(R.id.logWaterUsageButton); // Button for logging water usage to db
        // Making the action buttons actually clickable
        LinearLayout showerButton = view.findViewById(R.id.showerButton);
        LinearLayout washerButton = view.findViewById(R.id.washerButton);
        LinearLayout sprinklerButton = view.findViewById(R.id.sprinklerButton);
        LinearLayout otherOption = view.findViewById(R.id.otherOption);

        showerButton.setOnClickListener(v -> setSelectedActivity(showerButton, "Shower"));
        washerButton.setOnClickListener(v -> setSelectedActivity(washerButton, "Washer"));
        sprinklerButton.setOnClickListener(v -> setSelectedActivity(sprinklerButton, "Sprinkler"));

        incrementButton.setOnClickListener(v -> {
            timerValue++;
            updateTimerLabel();
        });

        decrementButton.setOnClickListener(v -> {
            if (timerValue > 0) {
                timerValue--;
                updateTimerLabel();
            }
        });

        otherOption.setOnClickListener(v -> {
            String[] otherActivities = {"Washing Car", "Watering Garden", "Cleaning", "Filling Pool"};
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Select Activity");
            builder.setItems(otherActivities, (dialog, which) -> {
                String selected = otherActivities[which];
                setSelectedActivity(otherOption, selected);
                Toast.makeText(requireContext(), "Selected: " + selected, Toast.LENGTH_SHORT).show();
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
        });


        new Thread(() -> {
            db  = AppDatabase.getInstance(requireContext());
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

            goalType = user.getGoalType() != null ? user.getGoalType().toString() : "DAILY"; // default fallback

            float totalUsagePre;
            if ("DAILY".equals(goalType)) {
                long[] todayWindow = computeTodayWindow();
                Float usage = waterUsageDao.getLitersUsedBetween(userEmail, todayWindow[0], todayWindow[1]);
                totalUsagePre = usage != null ? usage : 0f;
            } else if ("WEEKLY".equals(goalType)) {
                long[] weekWindow = computeCurrentWeekWindow();
                Float usage = waterUsageDao.getLitersUsedBetween(userEmail, weekWindow[0], weekWindow[1]);
                totalUsagePre = usage != null ? usage : 0f;
            } else {
                long[] monthWindow = computeCurrentMonthWindow();
                Float usage = waterUsageDao.getLitersUsedBetween(userEmail, monthWindow[0], monthWindow[1]);
                totalUsagePre = usage != null ? usage : 0f;
            }

            List<GoalProgress> goalProgressList = goalProgressDao.getAllProgressForUser(userEmail);
            Log.d("HomeFragment", "Goal progress list size: " + goalProgressList.size());
            for (GoalProgress gp : goalProgressList) {
                Log.d("HomeFragment", "GoalProgress: " + gp.toString());
            }

            if (goalProgressList.isEmpty()) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "No goal progress found.", Toast.LENGTH_LONG).show());
                GoalProgress defaultGoal = new GoalProgress(userEmail,0f, new Date(), true, 100);
                goalProgressDao.insertGoalProgress(defaultGoal);
                goalProgressList = goalProgressDao.getAllProgressForUser(userEmail);
                return;
            }

            float goal = goalProgressList.get(0).getGoalAmount();

            List<GoalProgress> finalGoalProgressList = goalProgressList;
            requireActivity().runOnUiThread(() -> {
                // safe to use db here
                updatePieChart(totalUsagePre, goal);

                logWaterUsage.setOnClickListener(v -> {
                    if (db == null) {
                        Toast.makeText(requireContext(), "Database is not ready yet. Please wait.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (selectedActivity == null || timerValue == 0) {
                        Toast.makeText(requireContext(), "Please select an activity and set the time.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double multiplier = 1;

                    /* Conditional series below define a variable called multiplier
                     * Multiplier value depends on average liters of water used per minute
                     * for a respective activity (determined via google search)

                     */

                    if (selectedActivity.equals("Shower"))
                    {
                        multiplier = 15;
                    }
                    else if (selectedActivity.equals("Sprinkler"))
                    {
                        multiplier = 17;
                    }
                    else if (selectedActivity.equals("Washer"))
                    {
                        multiplier = 0.13;
                    }
                    else if (selectedActivity.equals("Washing Car"))
                    {
                        multiplier = 10;
                    }
                    else if (selectedActivity.equals("Watering Garden"))
                    {
                        multiplier = 5;
                    }
                    else if (selectedActivity.equals("Cleaning"))
                    {
                        multiplier = 10;
                    }
                    else if (selectedActivity.equals("Filling Pool"))
                    {
                        multiplier = 10;
                    }


                    double litersUsed = timerValue * multiplier;
                    Date currentDate = new Date();


                    WaterUsage waterUsage = new WaterUsage(userEmail, currentDate, litersUsed, selectedActivity);

                    new Thread(() -> {
                        waterUsageDao.insertLog(waterUsage);
                        float totalUsage;
                        if ("DAILY".equals(goalType)) {
                            long[] todayWindow = computeTodayWindow();
                            Float usage = waterUsageDao.getLitersUsedBetween(userEmail, todayWindow[0], todayWindow[1]);
                            totalUsage = usage != null ? usage : 0f;
                        } else if ("WEEKLY".equals(goalType)) {
                            long[] weekWindow = computeCurrentWeekWindow();
                            Float usage = waterUsageDao.getLitersUsedBetween(userEmail, weekWindow[0], weekWindow[1]);
                            totalUsage = usage != null ? usage : 0f;
                        } else {
                            long[] monthWindow = computeCurrentMonthWindow();
                            Float usage = waterUsageDao.getLitersUsedBetween(userEmail, monthWindow[0], monthWindow[1]);
                            totalUsage = usage != null ? usage : 0f;
                        }

                        GoalProgress mostRecentGoal = finalGoalProgressList.get(0);
                        mostRecentGoal.setAmountLogged(totalUsage);
                        mostRecentGoal.setProgressDate(new Date());
                        mostRecentGoal.setOnTarget(totalUsage <= goal);

                        goalProgressDao.updateGoalProgress(mostRecentGoal);

                        requireActivity().runOnUiThread(() -> {
                            // Reset and update UI
                            timerValue = 0;
                            updateTimerLabel();
                            updatePieChart(totalUsage, goal);
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
    }

    private void updateTimerLabel() {
        timerLabel.setText("Timer: " + timerValue + " min");
    }

    private void updatePieChart(float usage, float goal) {
        ArrayList<PieEntry> entries = new ArrayList<>();

        // Convert gallons to liters
        float usageLiters = usage * 3.78541f;

        // Define total in liters
        float totalLiters = goal*3.78541f;

        float remaining = Math.max(totalLiters - usageLiters, 0);

        entries.add(new PieEntry(usage, "Used"));
        entries.add(new PieEntry(remaining, "Remaining"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(
                ContextCompat.getColor(requireContext(), R.color.light_blue),
                ContextCompat.getColor(requireContext(), R.color.dark_blue)
        );
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        String centerText;
        float percentUsed = goal != 0 ? (usageLiters / totalLiters) * 100f : 0f;

        if (goalType.equals("DAILY"))
        {
            pieChart.setCenterText(usage + " liters\n" + ((usage / goal) * goal) + "% used today");

        }
        else if (goalType.equals("WEEKLY")) {
            pieChart.setCenterText(usage + " liters\n" + ((usage / goal) * goal) + "% used this week");
        }
        else {
            pieChart.setCenterText(usage + " liters\n" + ((usage / goal) * goal) + "% used this month");
        }
        if ("DAILY".equals(goalType)) {
            centerText = String.format("%.1f liters\n%.1f%% used today", usageLiters, percentUsed);
        } else if ("WEEKLY".equals(goalType)) {
            centerText = String.format("%.1f liters\n%.1f%% used this week", usageLiters, percentUsed);
        } else {
            centerText = String.format("%.1f liters\n%.1f%% used this month", usageLiters, percentUsed);
        }

        pieChart.setCenterText(centerText);
        pieChart.setCenterTextSize(16f);
        pieChart.setCenterTextColor(Color.BLACK);
        pieChart.getDescription().setEnabled(false);
        pieChart.invalidate();
    }

    private void setSelectedActivity(LinearLayout layout, String activity) {
        if (selectedLayout!= null) {
            selectedLayout.setBackgroundResource(R.drawable.default_background);
        }

        layout.setBackgroundResource(R.drawable.selected_background);
        selectedLayout = layout;
        selectedActivity = activity;
    }

    // This method returns a two item array, where
    // the first item is the start of the day at 12 am and
    // the second item is the end of the day right before the next
    // 12 am.
    public static long[] computeTodayWindow() {
        Calendar cal = Calendar.getInstance();
        // start of today
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE,      0);
        cal.set(Calendar.SECOND,      0);
        cal.set(Calendar.MILLISECOND, 0);
        long start = cal.getTimeInMillis();

        // start of tomorrow
        cal.add(Calendar.DAY_OF_YEAR, 1);
        long end = cal.getTimeInMillis();

        return new long[]{ start, end };
    }


    // This method returns a two item array, where the first
    // item is the start of the current week and the second
    // is the end of that current week.

    // This method is fixed so the start of the week is the most
    // recent Monday and the end is the Sunday right before the next
    // Monday.
    public static long[] computeCurrentWeekWindow() {
        Calendar now = Calendar.getInstance();
        int todayDow = now.get(Calendar.DAY_OF_WEEK);
        int daysSinceMonday = (todayDow + 5) % 7;
        Calendar weekStart = (Calendar) now.clone();
        weekStart.add(Calendar.DAY_OF_YEAR, -daysSinceMonday);
        weekStart.set(Calendar.HOUR_OF_DAY, 0);
        weekStart.set(Calendar.MINUTE,      0);
        weekStart.set(Calendar.SECOND,      0);
        weekStart.set(Calendar.MILLISECOND, 0);
        long start = weekStart.getTimeInMillis();
        weekStart.add(Calendar.DAY_OF_YEAR, 7);
        long end = weekStart.getTimeInMillis();

        return new long[]{ start, end };
    }

    // This method returns a two item array, where the first
    // item is the start of the current month and the second
    // is the start of the next month
    public static long[] computeCurrentMonthWindow() {
        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY,    0);
        cal.set(Calendar.MINUTE,         0);
        cal.set(Calendar.SECOND,         0);
        cal.set(Calendar.MILLISECOND,    0);
        long startOfMonth = cal.getTimeInMillis();

        // Advance to first day of next month at midnight
        cal.add(Calendar.MONTH, 1);
        long startOfNextMonth = cal.getTimeInMillis();

        return new long[]{ startOfMonth, startOfNextMonth };
    }

}



