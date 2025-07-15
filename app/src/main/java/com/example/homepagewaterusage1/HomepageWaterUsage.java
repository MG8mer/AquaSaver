package com.example.homepagewaterusage1;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aquasaver.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;

public class HomepageWaterUsage extends AppCompatActivity {

    int timerValue = 0;
    TextView timerLabel;
    PieChart pieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage_water_usage);

        pieChart = findViewById(R.id.pieChart);
        timerLabel = findViewById(R.id.timerLabel);
        Button incrementButton = findViewById(R.id.incrementTimer);
        Button decrementButton = findViewById(R.id.decrementTimer);

        updatePieChart();

        incrementButton.setOnClickListener(v -> {
            timerValue += 10;
            updateTimerLabel();
            updatePieChart();
        });

        decrementButton.setOnClickListener(v -> {
            if (timerValue >= 10) {
                timerValue -= 10;
                updateTimerLabel();
                updatePieChart();
            }
        });

        // Popup for "Other" option
        LinearLayout otherOption = findViewById(R.id.otherOption);
        otherOption.setOnClickListener(v -> {
            String[] otherActivities = {"Washing Car", "Watering Garden", "Cleaning", "Filling Pool"};

            AlertDialog.Builder builder = new AlertDialog.Builder(HomepageWaterUsage.this);
            builder.setTitle("Select Activity");
            builder.setItems(otherActivities, (dialog, which) -> {
                String selected = otherActivities[which];
                Toast.makeText(this, "Selected: " + selected, Toast.LENGTH_SHORT).show();

                // Optional: Add logic to increase timer or update pie chart
            });

            builder.setNegativeButton("Cancel", null);
            builder.show();
        });
    }

    private void updateTimerLabel() {
        timerLabel.setText("Timer: " + timerValue + " min");
    }

    private void updatePieChart() {
        ArrayList<PieEntry> entries = new ArrayList<>();

        // Calculate usage in gallons
        float usageGallons = timerValue * 2f; // 2 gallons per minute

        // Convert gallons to liters
        float usageLiters = usageGallons * 3.78541f;

        // Define total in liters (100 gallons)
        float totalLiters = 378.541f;

        float remaining = Math.max(totalLiters - usageLiters, 0);

        entries.add(new PieEntry(usageLiters, "Used"));
        entries.add(new PieEntry(remaining, "Remaining"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{R.color.light_blue, R.color.dark_blue}, this);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        float percentUsed = (usageLiters / totalLiters) * 100;
        pieChart.setCenterText(String.format("%.1f liters\n%.1f%% used today", usageLiters, percentUsed));
        pieChart.setCenterTextSize(16f);
        pieChart.setCenterTextColor(Color.BLACK);
        pieChart.getDescription().setEnabled(false);
        pieChart.invalidate();
    }
}
