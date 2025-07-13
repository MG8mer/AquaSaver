package com.aquasaver;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import com.example.aquasaver.R;
import com.example.aquasaver.dao.GoalProgressDao;
import com.example.aquasaver.dao.UserProfileDao;
import com.example.aquasaver.dao.WaterUsageDao;
import com.example.aquasaver.db.AppDatabase;
import com.example.aquasaver.model.WaterUsage;
import com.example.aquasaver.model.enums.GoalType;
import com.example.aquasaver.model.enums.SummaryType;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.example.aquasaver.model.UserProfile;
import com.example.aquasaver.dao.UserProfileDao;
import com.example.aquasaver.model.GoalProgress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HomePageWaterUsage extends AppCompatActivity {

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage_water_usage);

        pieChart = findViewById(R.id.pieChart);
        timerLabel = findViewById(R.id.timerLabel);
        Button incrementButton = findViewById(R.id.incrementTimer);
        Button decrementButton = findViewById(R.id.decrementTimer);
        Button logWaterUsage = findViewById(R.id.logWaterUsageButton); // Button for logging water usage to db

        // Making the action buttons actually clickable
        LinearLayout showerButton = findViewById(R.id.showerButton);
        LinearLayout washerButton = findViewById(R.id.washerButton);
        LinearLayout sprinklerButton = findViewById(R.id.sprinklerButton);

        showerButton.setOnClickListener(v -> {
            setSelectedActivity(showerButton, "Shower");
        });

        washerButton.setOnClickListener(v -> {
            setSelectedActivity(washerButton, "Washer");
        });

        sprinklerButton.setOnClickListener(v -> {
            setSelectedActivity(sprinklerButton, "Sprinkler");
        });

        incrementButton.setOnClickListener(v -> {
            timerValue += 1;
            updateTimerLabel();
        });

        decrementButton.setOnClickListener(v -> {
            if (timerValue >= 1) {
                timerValue -= 1;
                updateTimerLabel();
            }
        });


        new Thread(() -> {
            AppDatabase db = Room.databaseBuilder(
                    getApplicationContext(),
                    AppDatabase.class,
                    "aqua_db"
            ).fallbackToDestructiveMigration().build();
            waterUsageDao = db.waterUsageDao();
            userProfileDao = db.userProfileDao();
            goalProgressDao = db.goalProgressDao();


            user = userProfileDao.getUserByEmail("bro@gmail.com"); // REPLACE bro@gmail.com PROPER LOGIC TO OBTAIN USER EMAIL
            goalType = user.getGoalType().toString();
            float totalUsagePre;

            // REPLACE bro@gmail.com PROPER LOGIC TO OBTAIN USER EMAIL
            if (goalType.equals("DAILY"))
            {
                long[] todayWindow = computeTodayWindow();
                totalUsagePre = waterUsageDao.getLitersUsedBetween("bro@gmail.com", todayWindow[0], todayWindow[1]);
            }
            else
            {
                long[] weekWindow = computeCurrentWeekWindow();
                totalUsagePre = waterUsageDao.getLitersUsedBetween("bro@gmail.com", weekWindow[0], weekWindow[1]);
            }
            List<GoalProgress> goalProgressList = goalProgressDao.getAllProgressForUser("bro@gmail.com");
            float goal = goalProgressList.get(0).getGoalAmount();
            runOnUiThread(() -> {
                // safe to use db here
                updatePieChart(totalUsagePre, goal);

                logWaterUsage.setOnClickListener(v -> {
                    if (db == null) {
                        Toast.makeText(this, "Database is not ready yet. Please wait.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (selectedActivity == null || timerValue == 0) {
                        Toast.makeText(this, "Please select an activity and set the time.", Toast.LENGTH_SHORT).show();
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

                    double litersUsed = timerValue * multiplier;
                    Date currentDate = new Date();


                    // REPLACE BELOW LINE WITH LOGIC TO OBTAIN ACTUAL USER CREDENTIALS
                    WaterUsage waterUsage = new WaterUsage("bro@gmail.com", currentDate, litersUsed, selectedActivity);

                    new Thread(() -> {
                        waterUsageDao.insertLog(waterUsage);
                        float totalUsage;
                        // REPLACE bro@gmail.com PROPER LOGIC TO OBTAIN USER EMAIL
                        if (goalType.equals("DAILY")){
                            long[] todayWindow = computeTodayWindow();
                            Float totalWrapper = waterUsageDao.getLitersUsedBetween("bro@gmail.com", todayWindow[0], todayWindow[1]);
                            totalUsage  = (totalWrapper != null) ? totalWrapper : 0f;
                        }
                        else {
                            long[] weekWindow = computeCurrentWeekWindow();
                            Float totalWrapper = waterUsageDao.getLitersUsedBetween("bro@gmail.com", weekWindow[0], weekWindow[1]);
                            totalUsage  = (totalWrapper != null) ? totalWrapper : 0f;                        }

                        GoalProgress mostRecentGoal = goalProgressList.get(0);
                        mostRecentGoal.setAmountLogged(totalUsage);
                        mostRecentGoal.setProgressDate(new Date());


                        runOnUiThread(() -> {
                            // Reset and update UI
                            timerValue = 0;
                            updateTimerLabel();
                            updatePieChart(totalUsage, goal);
                            Toast.makeText(this, "Water usage logged", Toast.LENGTH_SHORT).show();

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
                ContextCompat.getColor(this, R.color.light_blue),
                ContextCompat.getColor(this, R.color.dark_blue)
        );
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        if (goalType.equals("DAILY"))
        {
            pieChart.setCenterText(usage + " liters\n" + ((usage / goal) * goal) + "% used today");

        }
        else {
            pieChart.setCenterText(usage + " liters\n" + ((usage / goal) * goal) + "% used this week");
        }
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

}

