package com.example.homepagewaterusage1;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HomepageWaterUsage extends AppCompatActivity {

    private Spinner otherDropdown;
    private ImageView otherIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage_water_usage);

        otherDropdown = findViewById(R.id.otherDropdown);
        otherIcon = findViewById(R.id.otherIcon);

        otherIcon.setOnClickListener(v -> {
            if (otherDropdown.getVisibility() == View.GONE) {
                otherDropdown.setVisibility(View.VISIBLE);
            } else {
                otherDropdown.setVisibility(View.GONE);
            }
        });

        otherDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                Toast.makeText(HomepageWaterUsage.this, "Selected: " + selected, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
}
