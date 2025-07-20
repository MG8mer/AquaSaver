package com.example.aquasaver.ui.water_usage;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.aquasaver.dao.GoalProgressDao;
import com.example.aquasaver.dao.UserProfileDao;
import com.example.aquasaver.dao.WaterUsageDao;
import com.example.aquasaver.db.AppDatabase;
import com.example.aquasaver.model.GoalProgress;
import com.example.aquasaver.model.UserProfile;
import com.example.aquasaver.model.WaterUsage;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.google.android.material.button.MaterialButtonToggleGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.aquasaver.databinding.FragmentWaterUsageBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import com.example.aquasaver.R;

public class WaterUsageFragment extends Fragment {

    Calendar curCalendar = Calendar.getInstance();
    Calendar calendar = Calendar.getInstance();

    private FragmentWaterUsageBinding binding;

    private WaterUsageDao dao;

    private UserProfile user;

    private String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    private String[] dataRange;

    private String viewRange;

    private List<DailyUsage> data = new ArrayList<>();

    private BarChart barChart;

    float avg;

    float target;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        WaterUsageViewModel waterusageViewModel =
                new ViewModelProvider(this).get(WaterUsageViewModel.class);

        binding = FragmentWaterUsageBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Context context = requireContext(); // or getContext(), if you know it's non-null
        AppDatabase db = AppDatabase.getInstance(context);

        dao = db.waterUsageDao();
        UserProfileDao userProfileDao = db.userProfileDao();
        GoalProgressDao goalProgressDao = db.goalProgressDao();

        new Thread(() -> {
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

            GoalProgress goalProgress = goalProgressDao.getTodayProgress(userEmail);
            float targetValue = goalProgress != null ? goalProgress.getGoalAmount() : 100f;
            target = targetValue;

            //addRandomLogsForDateRange(dao);
            float litersUsed = dao.getLitersUsedToday(user.getEmail());
            Log.d("TEST", "litersUsed: " + litersUsed);
            List<DailyUsage> usageData = dao.getAllDailyUsageForUser(user.getEmail());

            requireActivity().runOnUiThread(() -> {
                barChart = binding.waterGraph;;
                binding.graphToggle.check(R.id.week);
                viewRange = "week";
                data = usageData;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                dataRange = setDateRange(sdf.format(curCalendar.getTime()), viewRange);
                List<DailyUsage> sublist = getSublistByViewRange(data, dataRange);
                List<BarEntry> entries = convertDailyUsageToBarEntries(sublist);
                Log.d("TEST", "dataRange: " + Arrays.toString(dataRange));
                createBarGraphWithDailyUsage(entries, viewRange, dataRange);
            });
        }).start();


        binding.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backPress();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                List<DailyUsage> sublist = getSublistByViewRange(data, dataRange);
                List<BarEntry> entries = convertDailyUsageToBarEntries(sublist);
                Log.d("TEST", "dataRange: " + Arrays.toString(dataRange));
                createBarGraphWithDailyUsage(entries, viewRange, dataRange);
            }
        });

        binding.buttonForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forwardPress();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                List<DailyUsage> sublist = getSublistByViewRange(data, dataRange);
                List<BarEntry> entries = convertDailyUsageToBarEntries(sublist);
                Log.d("TEST", "dataRange: " + Arrays.toString(dataRange));
                createBarGraphWithDailyUsage(entries, viewRange, dataRange);
            }
        });



        binding.graphToggle.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (isChecked) {
                    if (checkedId == R.id.day) {
                        viewRange = "day";
                    } else if (checkedId == R.id.week) {
                        viewRange = "week";
                    } else if (checkedId == R.id.month) {
                        viewRange = "month";
                    } else if (checkedId == R.id.year) {
                        viewRange = "year";
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    dataRange = setDateRange(sdf.format(curCalendar.getTime()), viewRange);
                    List<DailyUsage> sublist = getSublistByViewRange(data, dataRange);
                    List<BarEntry> entries = convertDailyUsageToBarEntries(sublist);
                    Log.d("TEST", "dataRange: " + Arrays.toString(dataRange));
                    createBarGraphWithDailyUsage(entries, viewRange, dataRange);
                }
            }
        });

        return root;
    }

    public void backPress() {
        if (dataRange == null || dataRange.length < 2) return;

        // Save old dates to revert if needed
        String oldStart = dataRange[0];
        String oldEnd = dataRange[1];

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();

        try {
            startCal.setTime(sdf.parse(dataRange[0]));
            endCal.setTime(sdf.parse(dataRange[1]));
        } catch (ParseException e) {
            e.printStackTrace();
            return;  // Parsing failed, exit
        }

        switch (viewRange) {
            case "day":
                startCal.add(Calendar.DAY_OF_MONTH, -1);
                endCal.setTime(startCal.getTime());
                break;
            case "week":
                startCal.add(Calendar.WEEK_OF_YEAR, -1);
                endCal.add(Calendar.WEEK_OF_YEAR, -1);
                break;
            case "month":
                startCal.add(Calendar.MONTH, -1);
                endCal.add(Calendar.MONTH, -1);
                break;
            case "year":
                startCal.add(Calendar.YEAR, -1);
                endCal.add(Calendar.YEAR, -1);
                break;
            default:
                return;
        }

        dataRange[0] = sdf.format(startCal.getTime());
        dataRange[1] = sdf.format(endCal.getTime());

        // Check if both dates are valid indexes
        int startIndex = findDailyUsageIndex(data, dataRange[0]);
        int endIndex = findDailyUsageIndex(data, dataRange[1]);

        if (startIndex == -1 && endIndex == -1) {
            // Revert changes because both dates not found
            dataRange[0] = oldStart;
            dataRange[1] = oldEnd;
        }
    }

    public boolean isLeapYear(String dateString) {
        try {
            // Extract the year from the date string (e.g., "2024-02-29")
            int year = Integer.parseInt(dateString.substring(0, 4));

            // Leap year logic
            return (year % 4 == 0) && ((year % 100 != 0) || (year % 400 == 0));
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Invalid date format
        }
    }

    public void forwardPress() {
        if (dataRange == null || dataRange.length < 2) return;

        // Save old dates to revert if needed
        String oldStart = dataRange[0];
        String oldEnd = dataRange[1];

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();

        try {
            startCal.setTime(sdf.parse(dataRange[0]));
            endCal.setTime(sdf.parse(dataRange[1]));
        } catch (ParseException e) {
            e.printStackTrace();
            return;  // Parsing failed, exit
        }

        switch (viewRange) {
            case "day":
                startCal.add(Calendar.DAY_OF_MONTH, 1);
                endCal.setTime(startCal.getTime());
                break;

            case "week":
                startCal.add(Calendar.WEEK_OF_YEAR, 1);
                endCal.add(Calendar.WEEK_OF_YEAR, 1);
                break;

            case "month":
                startCal.add(Calendar.MONTH, 1);
                endCal.add(Calendar.MONTH, 1);
                break;

            case "year":
                startCal.add(Calendar.YEAR, 1);
                endCal.add(Calendar.YEAR, 1);
                break;

            default:
                return;
        }

        dataRange[0] = sdf.format(startCal.getTime());
        dataRange[1] = sdf.format(endCal.getTime());

        // Check if both dates are valid indexes
        int startIndex = findDailyUsageIndex(data, dataRange[0]);
        int endIndex = findDailyUsageIndex(data, dataRange[1]);

        if (startIndex == -1 && endIndex == -1) {
            // Revert changes because both dates not found
            dataRange[0] = oldStart;
            dataRange[1] = oldEnd;
        }
    }

    public boolean isDateWithinRange(String currentDate) {
        if (currentDate == null || dataRange == null || dataRange.length < 2) return false;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        try {
            Date current = sdf.parse(currentDate);
            Date start = sdf.parse(dataRange[0]);
            Date end = sdf.parse(dataRange[1]);

            // Check if current >= start && current <= end
            return !current.before(start) && !current.after(end);

        } catch (ParseException e) {
            e.printStackTrace();
            return false;  // Parsing failed means no match
        }
    }

    private void createBarGraphWithDailyUsage(List<BarEntry> dataToUse, String timeWindow, String[] dataRange) {
        if (barChart == null || dataToUse == null || dataToUse.isEmpty()) return;

        barChart.clear();
        float targetValue = (float) (viewRange == "year" ? (isLeapYear(dataRange[0]) ? target*366.0/12.0 : target*365/12.0) : target);

        YAxis rightAxis = barChart.getAxisRight();
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.removeAllLimitLines();
        rightAxis.removeAllLimitLines();


        TypedValue typedValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        int colorPrimary = typedValue.data;

        // Set up bar dataset
        BarDataSet dataSet = new BarDataSet(dataToUse, "Water Usage");
        dataSet.setColor(colorPrimary);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        // Configure axes

        leftAxis.setAxisMinimum(0f);
        rightAxis.setAxisMinimum(0f);

        float maxY = getMaxY(dataToUse);  // Custom helper to find max usage
        float upperLimit = Math.max(Math.max(avg, targetValue), maxY) + 10;
        leftAxis.setAxisMaximum(upperLimit);
        rightAxis.setAxisMaximum(upperLimit);


        final String[] labels;
        String labelText;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date now = new Date();
        String today = sdf.format(now);

        switch (timeWindow) {
            case "day":
                labels = new String[] {formatDateLabel(dataRange[0])};

                if(isDateWithinRange(today)) {
                    labelText = "Today";
                } else {
                    labelText = formatDateLabel(dataRange[0]);
                }
                break;

            case "week":
                labels = days;
                if(isDateWithinRange(today)) {
                    labelText = "This Week";
                } else {
                    labelText = formatDateLabel(dataRange[0]) + " - " + formatDateLabel(dataRange[1]);

                }
                break;

            case "month":
                labels = new String[] {
                        "1", "", "", "", "", "", "",
                        "8", "", "", "", "", "", "",
                        "15", "", "", "", "", "", "",
                        "22", "", "", "", "", "", "",
                        "29", "", "", "", "", "", "",
                        "", ""
                };
                labelText = formatMonthLabel(dataRange[0]);
                break;

            case "year":
                labels = months;
                labelText = formatYearLabel(dataRange[0]);
                break;

            default:
                labels = new String[dataToUse.size()];
                Arrays.fill(labels, "");
                labelText = "";
                break;
        }

        setLabelText(labelText);

        Log.d("DEBUG", "MADE IT");
        // 6. Configure X-axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(100, false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));

        int targetColor = Color.argb(225, 0, 255, 0);

        int avgColor = Color.argb(128, 0, 0, 0);
        // 5. Add average and target limit lines
        LimitLine avgLine = new LimitLine(avg, "avg");
        avgLine.setLineColor(avgColor);
        avgLine.setLineWidth(2f);
        avgLine.setTextColor(avgColor);
        avgLine.setTextSize(12f);
        avgLine.enableDashedLine(10f, 10f, 0f);

        LimitLine targetLine = new LimitLine(targetValue, "target");
        targetLine.setLineColor(targetColor);
        targetLine.setLineWidth(2f);
        targetLine.setTextSize(12f);
        targetLine.setTextColor(Color.BLACK);
        targetLine.enableDashedLine(15f, 10f, 0f);

        leftAxis.addLimitLine(avgLine);
        leftAxis.addLimitLine(targetLine);

        // 7. Enable both Y axes
        leftAxis.setEnabled(true);
        rightAxis.setEnabled(true);

        barChart.getBarData().setDrawValues(false);
        barChart.getDescription().setEnabled(false);
        barChart.setExtraBottomOffset(16f);
        barChart.invalidate();
    }

    // Helper function to get max y from BarEntry list
    private float getMaxY(List<BarEntry> entries) {
        float max = 0f;
        for (BarEntry entry : entries) {
            if (entry.getY() > max) max = entry.getY();
        }
        return max;
    }


    public List<BarEntry> convertDailyUsageToBarEntries(List<DailyUsage> dailyUsageList) {
        if (dailyUsageList == null || dailyUsageList.isEmpty()) {
            return new ArrayList<>();
        }

        // First, sort the list by date ascending (assuming format YYYY-MM-DD lex order works)
        Collections.sort(dailyUsageList, new Comparator<DailyUsage>() {
            @Override
            public int compare(DailyUsage d1, DailyUsage d2) {
                return d1.getDay().compareTo(d2.getDay());
            }
        });

        List<BarEntry> barEntries = new ArrayList<>();

        for (int i = 0; i < dailyUsageList.size(); i++) {
            DailyUsage dailyUsage = dailyUsageList.get(i);
            float liters = dailyUsage.getUsage();
            barEntries.add(new BarEntry(i, liters));
        }

        return barEntries;
    }

    // Converts "YYYY-MM-DD" to "Jul 19"
    private String formatDateLabel(String date) {
        // date format YYYY-MM-DD
        String[] parts = date.split("-");
        if (parts.length != 3) return date; // fallback

        int month = Integer.parseInt(parts[1]);
        int day = Integer.parseInt(parts[2]);

        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

        return months[month - 1] + " " + day;
    }

    // Converts "YYYY-MM-DD" to "Jul"
    private String formatMonthLabel(String date) {
        String[] parts = date.split("-");
        if (parts.length != 3) return date;

        int month = Integer.parseInt(parts[1]);
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        return months[month - 1];
    }

    // Converts "YYYY-MM-DD" to "YYYY"
    private String formatYearLabel(String date) {
        String[] parts = date.split("-");
        if (parts.length != 3) return date;

        return parts[0];
    }
    public List<DailyUsage> getSublistByViewRange(List<DailyUsage> dailyUsageList, String[] dataRange) {
        String startDate = dataRange[0];
        String endDate = dataRange[1];

        List<DailyUsage> result = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();

        try {
            startCal.setTime(sdf.parse(startDate));
            endCal.setTime(sdf.parse(endDate));
        } catch (ParseException e) {
            e.printStackTrace();
            return result;
        }

        if (dailyUsageList == null || dailyUsageList.isEmpty()) {
            return result;
        }

        if ("year".equals(viewRange)) {
            // Initialize array for 12 months (0 = Jan, ..., 11 = Dec)
            float[] monthSums = new float[12];
            boolean[] monthSeen = new boolean[12];

            float low = Float.MAX_VALUE;
            float high = Float.MIN_VALUE;
            float sum = 0f;
            int count = 0;

            for (DailyUsage du : dailyUsageList) {
                try {
                    Date duDate = sdf.parse(du.getDay());
                    if (!duDate.before(startCal.getTime()) && !duDate.after(endCal.getTime())) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(duDate);
                        int month = cal.get(Calendar.MONTH);
                        monthSums[month] += du.getUsage();
                        monthSeen[month] = true;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            for (int i = 0; i < 12; i++) {
                String labelDate = String.format(Locale.US, "%04d-%02d-01", startCal.get(Calendar.YEAR), i + 1);
                float monthlyTotal = monthSums[i];

                result.add(new DailyUsage(labelDate, monthlyTotal));

                if (monthSeen[i]) {
                    low = Math.min(low, monthlyTotal);
                    high = Math.max(high, monthlyTotal);
                    sum += monthlyTotal;
                    count++;
                }
            }

            Float newAvg = (count > 0) ? (sum / count) : null;
            avg = newAvg == null ? 0 : newAvg;
            setStatistics(low, high, newAvg);

            Log.d("UsageStats", "Low (monthly): " + low + ", High (monthly): " + high + ", Avg (monthly): " + avg);
            return result;
        }

        // Otherwise, default (daily) behavior
        Map<String, DailyUsage> usageMap = new HashMap<>();
        float low = Float.MAX_VALUE;
        float high = Float.MIN_VALUE;
        float sum = 0f;
        int count = 0;

        for (DailyUsage du : dailyUsageList) {
            usageMap.put(du.getDay(), du);
            try {
                Date duDate = sdf.parse(du.getDay());
                if (!duDate.before(startCal.getTime()) && !duDate.after(endCal.getTime())) {
                    float usage = du.getUsage();
                    low = Math.min(low, usage);
                    high = Math.max(high, usage);
                    sum += usage;
                    count++;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        Float newAvg = (count > 0) ? (sum / count) : null;
        avg = newAvg == null ? 0 : newAvg;
        setStatistics(low, high, newAvg);

        Log.d("UsageStats", "Low: " + low + ", High: " + high + ", Avg: " + avg);

        Calendar iterCal = (Calendar) startCal.clone();
        while (!iterCal.after(endCal)) {
            String dateStr = sdf.format(iterCal.getTime());
            DailyUsage du = usageMap.get(dateStr);
            if (du != null) {
                result.add(new DailyUsage(du));
            } else {
                result.add(new DailyUsage(dateStr, 0f));
            }
            iterCal.add(Calendar.DAY_OF_MONTH, 1);
        }

        return result;
    }


    public String[] setDateRange(String currentDay, String viewRange) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar cal = Calendar.getInstance();

        try {
            Date date = sdf.parse(currentDay);
            cal.setTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return new String[] {"", ""};
        }

        String startDate, endDate;

        switch (viewRange.toLowerCase()) {
            case "day":
                startDate = currentDay;
                endDate = currentDay;
                break;

            case "week":
                cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek()); // typically Sunday
                startDate = sdf.format(cal.getTime());
                cal.add(Calendar.DAY_OF_WEEK, 6);
                endDate = sdf.format(cal.getTime());
                break;

            case "month":
                cal.set(Calendar.DAY_OF_MONTH, 1);
                startDate = sdf.format(cal.getTime());
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                endDate = sdf.format(cal.getTime());
                break;

            case "year":
                cal.set(Calendar.DAY_OF_YEAR, 1);
                startDate = sdf.format(cal.getTime());
                cal.set(Calendar.DAY_OF_YEAR, cal.getActualMaximum(Calendar.DAY_OF_YEAR));
                endDate = sdf.format(cal.getTime());
                break;

            default:
                startDate = currentDay;
                endDate = currentDay;
                break;
        }

        return new String[] {startDate, endDate};
    }

    public int findDailyUsageIndex(List<DailyUsage> dailyUsageList, String targetDay) {
        if (dailyUsageList == null || targetDay == null) {
            return -1;
        }
        for (int i = 0; i < dailyUsageList.size(); i++) {
            DailyUsage dailyUsage = dailyUsageList.get(i);
            if (targetDay.equals(dailyUsage.getDay())) {  // assuming getDay() returns a String like "YYYY-MM-DD"
                return i;
            }
        }
        return -1; // not found
    }

    public void addRandomLogsForDateRange(WaterUsageDao waterUsageDao) {
        // Date format to parse and format dates
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();

        Date todayDate = null; // zero time part
        try {
            todayDate = sdf.parse(sdf.format(startCal.getTime()));
        } catch (ParseException e) {
            throw new RuntimeException(e);

        }
        long todayTimestamp = todayDate.getTime();
        dao.deleteLogsOlderThan(todayTimestamp);


        try {
            // Start date: Jan 1, 2025
            startCal.setTime(sdf.parse("2025-01-01"));
        } catch (ParseException e) {
            e.printStackTrace();
            return; // Abort on parse error
        }

        // End date: yesterday
        endCal.add(Calendar.DAY_OF_MONTH, -1);



        Random random = new Random();


        while (!startCal.after(endCal)) {
            // Convert current day to timestamp (milliseconds)
            long timestamp = startCal.getTimeInMillis();

            // Generate a random float value for usage (e.g., between 10.0 and 50.0)
            float randomUsage = (float) (target*0.1 + random.nextFloat() * 1.2*target);

            // Create a WaterUsage object — adapt constructor/fields as needed
            WaterUsage log = new WaterUsage(user.getEmail(), new Date(timestamp), randomUsage, "test");
            dao.insertLog(log);

            // Move to next day
            startCal.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    public void setLabelText(String s) {
        binding.labelText.setText(s);
    }

    public void setStatistics(float low, float high, Float avg) {
        binding.statistics.low.setText("Low: " + String.format("%.2f", low));
        binding.statistics.high.setText("High: " + String.format("%.2f", high));
        binding.statistics.avg.setText("Avg: " + String.format("%.2f", avg));
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
