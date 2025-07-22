package com.example.aquasaver.ui.water_usage;

public class DailyUsage {
    public String day;         // e.g. "2025-07-19"
    public float total_liters; // total liters used that day

    public DailyUsage(DailyUsage other) {
        this.day = other.day;
        this.total_liters = other.total_liters;
    }

    public DailyUsage(String day, float total_liters) {
        this.day = day;
        this.total_liters = total_liters;
    }

    public float getUsage() {
        return total_liters;
    }


    public String getDay() {
        return day;
    }
}
