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

import java.util.ArrayList;
import java.util.Date;

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
            if (timerValue >= 10) {
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

            runOnUiThread(() -> {
                // safe to use db here
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
                    WaterUsage waterUsage = new WaterUsage("example@gmail.com", currentDate, litersUsed, selectedActivity);

                    new Thread(() -> {
                        waterUsageDao.insertLog(waterUsage);

                        float totalUsage = waterUsageDao.getLitersUsedToday("example@gmail.com");


                        runOnUiThread(() -> {
                            // Reset and update UI
                            timerValue = 0;
                            updateTimerLabel();
                            updatePieChart(totalUsage);
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

    private void updatePieChart(float usage) {
        ArrayList<PieEntry> entries = new ArrayList<>();

        // Convert gallons to liters
        float usageLiters = usage * 3.78541f;

        // Define total in liters (100 gallons)
        float totalLiters = 378.541f;

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

        pieChart.setCenterText(usage + " gallons\n" + ((usage / 100) * 100) + "% used today");
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
}
