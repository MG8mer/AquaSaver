package com.example.homepagewaterusage1;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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
    }

    private void updateTimerLabel() {
        timerLabel.setText("Timer: " + timerValue + " min");
    }

    private void updatePieChart() {
        ArrayList<PieEntry> entries = new ArrayList<>();
        float usage = timerValue * 2f; // Example: 2 gallons per minute
        float remaining = Math.max(100 - usage, 0);

        entries.add(new PieEntry(usage, "Used"));
        entries.add(new PieEntry(remaining, "Remaining"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{R.color.light_blue, R.color.dark_blue}, this);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        pieChart.setCenterText(usage + " gallons\n" + ((usage/100)*100) + "% used today");
        pieChart.setCenterTextSize(16f);
        pieChart.setCenterTextColor(Color.BLACK);

        pieChart.getDescription().setEnabled(false);
        pieChart.invalidate();
    }
}
