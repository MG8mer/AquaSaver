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
import android.widget.TextView;
import android.widget.Toast;

import com.example.aquasaver.repository.GoalProgressRepository;
import com.example.aquasaver.repository.UserProfileRepository;
import com.example.aquasaver.repository.WaterUsageRepository;
import com.example.aquasaver.ui.home.HomeFragment;
import com.example.aquasaver.model.GoalProgress;
import com.example.aquasaver.model.UserProfile;
import com.example.aquasaver.model.WaterUsage;
import com.example.aquasaver.model.enums.GoalType;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.button.MaterialButtonToggleGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.aquasaver.databinding.FragmentWaterUsageBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

import com.example.aquasaver.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class WaterUsageFragment extends Fragment {

    Calendar curCalendar = Calendar.getInstance();
    Calendar calendar = Calendar.getInstance();

    private FragmentWaterUsageBinding binding;

    private WaterUsageRepository repo;
    private UserProfileRepository userProfileRepo;
    private GoalProgressRepository goalProgressRepo;

    private UserProfile user;

    private final String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private final String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    private final int[] daysInMonth = {31, calendar.get(Calendar.YEAR) % 4 == 0 ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    private String[] dataRange;
    private String viewRange;
    private List<DailyUsage> data = new ArrayList<>();
    private BarChart barChart;

    float avg;
    float target;
    float goalValue;
    GoalType goalType;

    // small helper to avoid repeating log tag
    private static final String TAG = "WaterUsageFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        WaterUsageViewModel waterusageViewModel =
                new ViewModelProvider(this).get(WaterUsageViewModel.class);

        binding = FragmentWaterUsageBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Context context = requireContext();

        repo = new WaterUsageRepository();
        userProfileRepo = new UserProfileRepository();
        goalProgressRepo = new GoalProgressRepository();

        // set defaults
        viewRange = "week";
        binding.graphToggle.check(R.id.week);

        loadAllDataAndBuildUI();

        // navigation buttons
        binding.buttonBack.setOnClickListener(v -> {
            backPress();
            refreshChartFromCurrentData();
        });
        binding.buttonForward.setOnClickListener(v -> {
            forwardPress();
            refreshChartFromCurrentData();
        });

        binding.graphToggle.addOnButtonCheckedListener((MaterialButtonToggleGroup.OnButtonCheckedListener) (group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.day) viewRange = "day";
            else if (checkedId == R.id.week) viewRange = "week";
            else if (checkedId == R.id.month) viewRange = "month";
            else if (checkedId == R.id.year) viewRange = "year";

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            dataRange = setDateRange(sdf.format(curCalendar.getTime()), viewRange);
            refreshChartFromCurrentData();
        });

        return root;
    }

    /**
     * Top-level loader: fetch user -> goalProgress -> daily usages -> build chart
     */
    private void loadAllDataAndBuildUI() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
        String userEmail = prefs.getString("username", null);

        if (userEmail == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_LONG).show();
            return;
        }

        // 1) get user
        userProfileRepo.getUserByEmail(userEmail, (QuerySnapshot userSnapshot) -> {
            if (userSnapshot == null || userSnapshot.isEmpty()) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "User not found. Please log in.", Toast.LENGTH_LONG).show());
                return;
            }

            DocumentSnapshot userDoc = userSnapshot.getDocuments().get(0);
            user = userDoc.toObject(UserProfile.class);
            if (user == null) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "User not found. Please log in.", Toast.LENGTH_LONG).show());
                return;
            }

            // 2) load goalProgress for current month (async)
            long[] monthlyWindow = HomeFragment.computeCurrentMonthWindow();
            goalProgressRepo.getMonthlyProgress(user.getEmail(), new Date(monthlyWindow[0]), new Date(monthlyWindow[1]), (QuerySnapshot gpSnapshot) -> {
                GoalProgress gp = null;
                if (gpSnapshot != null && !gpSnapshot.isEmpty()) {
                    gp = gpSnapshot.getDocuments().get(0).toObject(GoalProgress.class);
                }

                goalValue = (gp != null) ? gp.getGoalAmount() : 100f;
                goalType = (user.getGoalType() != null) ? user.getGoalType() : GoalType.DAILY;
                adjustTarget();

                // 3) load all daily usage for user
                repo.getAllDailyUsageForUser(user.getEmail(), (QuerySnapshot usageSnapshot) -> {
                    List<DailyUsage> usageList = new ArrayList<>();
                    if (usageSnapshot != null && !usageSnapshot.isEmpty()) {
                        for (DocumentSnapshot doc : usageSnapshot.getDocuments()) {
                            DailyUsage du = doc.toObject(DailyUsage.class);
                            if (du != null) usageList.add(du);
                        }
                    }
                    // assign to field and update UI on main thread
                    data = usageList;
                    requireActivity().runOnUiThread(() -> {
                        // initial viewRange already set
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                        dataRange = setDateRange(sdf.format(curCalendar.getTime()), viewRange);
                        List<DailyUsage> sublist = getSublistByViewRange(data, dataRange);
                        List<BarEntry> entries = convertDailyUsageToBarEntries(sublist);
                        barChart = binding.waterGraph;
                        createBarGraphWithDailyUsage(entries, viewRange, dataRange);
                    });
                });
            });
        });
    }

    // small helper to regenerate chart using current data/dataRange/viewRange
    private void refreshChartFromCurrentData() {
        if (data == null) return;
        if (dataRange == null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            dataRange = setDateRange(sdf.format(curCalendar.getTime()), viewRange);
        }
        List<DailyUsage> sublist = getSublistByViewRange(data, dataRange);
        List<BarEntry> entries = convertDailyUsageToBarEntries(sublist);
        createBarGraphWithDailyUsage(entries, viewRange, dataRange);
    }

    public void backPress() {
        if (dataRange == null || dataRange.length < 2) return;

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
            return;
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
                adjustTarget();
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

        int startIndex = findDailyUsageIndex(data, dataRange[0]);
        int endIndex = findDailyUsageIndex(data, dataRange[1]);

        if (startIndex == -1 && endIndex == -1) {
            dataRange[0] = oldStart;
            dataRange[1] = oldEnd;
        }
    }

    public boolean isLeapYear(String dateString) {
        try {
            int year = Integer.parseInt(dateString.substring(0, 4));
            return (year % 4 == 0) && ((year % 100 != 0) || (year % 400 == 0));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void adjustTarget() {
        float targetValue = goalValue;
        switch (goalType) {
            case DAILY:
                // daily target = goalValue (no change)
                break;
            case WEEKLY:
                targetValue = targetValue / 7.0f;
                break;
            case MONTHLY:
                targetValue = targetValue / daysInMonth[calendar.get(Calendar.MONTH)];
                break;
            default:
                break;
        }
        target = targetValue;
    }

    public void forwardPress() {
        if (dataRange == null || dataRange.length < 2) return;

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
            return;
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
                adjustTarget();
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

        int startIndex = findDailyUsageIndex(data, dataRange[0]);
        int endIndex = findDailyUsageIndex(data, dataRange[1]);
        String today = sdf.format(Calendar.getInstance().getTime());

        if (startIndex == -1 && endIndex == -1 && !(oldEnd.compareTo(sdf.format(Calendar.getInstance().getTime())) < 0)) {
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
            return !current.before(start) && !current.after(end);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void createBarGraphWithDailyUsage(List<BarEntry> dataToUse, String timeWindow, String[] dataRange) {
        if (binding == null || binding.waterGraph == null) return;
        if (dataToUse == null) dataToUse = new ArrayList<>();
        if (barChart == null) barChart = binding.waterGraph;

        barChart.clear();
        float targetValue = (float) ( "year".equals(viewRange) ? (isLeapYear(dataRange[0]) ? target*366.0/12.0 : target*365/12.0) : target);

        YAxis rightAxis = barChart.getAxisRight();
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.removeAllLimitLines();
        rightAxis.removeAllLimitLines();

        TypedValue typedValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        int colorPrimary = typedValue.data;

        BarDataSet dataSet = new BarDataSet(dataToUse, "Water Usage");
        dataSet.setColor(colorPrimary);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        leftAxis.setAxisMinimum(0f);
        rightAxis.setAxisMinimum(0f);

        float maxY = getMaxY(dataToUse);
        float upperLimit = Math.max(Math.max(avg, targetValue), maxY) + 20;
        leftAxis.setAxisMaximum(upperLimit);
        rightAxis.setAxisMaximum(upperLimit);

        final String[] labels;
        String labelText;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date now = new Date();
        String today = sdf.format(now);

        switch (timeWindow) {
            case "day":
                labels = new String[] { formatDateLabel(dataRange[0]) };
                labelText = isDateWithinRange(today) ? "Today" : formatDateLabel(dataRange[0]);
                break;
            case "week":
                labels = days;
                labelText = isDateWithinRange(today) ? "This Week" : formatDateLabel(dataRange[0]) + " - " + formatDateLabel(dataRange[1]);
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

        XAxis xAxis = barChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(100, false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));

        int targetColor = Color.argb(255, 229, 57, 53);
        int avgColor = Color.argb(128, 0, 0, 0);

        LimitLine avgLine = new LimitLine(avg, "");
        avgLine.setLineColor(avgColor);
        avgLine.setLineWidth(2f);
        avgLine.setTextColor(avgColor);
        avgLine.setTextSize(12f);
        avgLine.enableDashedLine(15f, 10f, 0f);

        LimitLine targetLine = new LimitLine(targetValue, "");
        targetLine.setLineColor(targetColor);
        targetLine.setLineWidth(2f);
        targetLine.setTextSize(12f);
        targetLine.setTextColor(Color.BLACK);
        targetLine.enableDashedLine(15f, 10f, 0f);

        leftAxis.addLimitLine(avgLine);
        leftAxis.addLimitLine(targetLine);

        leftAxis.setEnabled(true);
        rightAxis.setEnabled(true);

        setLegend("Water Usage", "Average", "Target", colorPrimary, avgColor, targetColor);
        barChart.setExtraBottomOffset(16f);
        barChart.setExtraLeftOffset(10f);
        barChart.setExtraRightOffset(10f);
        barChart.getLegend().setEnabled(false);
        if (barChart.getBarData() != null) barChart.getBarData().setDrawValues(false);
        barChart.getDescription().setEnabled(false);
        barChart.invalidate();
    }

    private float getMaxY(List<BarEntry> entries) {
        float max = 0f;
        if (entries == null) return max;
        for (BarEntry entry : entries) {
            if (entry.getY() > max) max = entry.getY();
        }
        return max;
    }

    public void setLegend(String dataLabel, String avgLabel, String targetLabel, int dataColor, int avgColor, int targetColor) {
        if (binding == null || binding.legend == null) return;
        TextView legendWaterUsageText = binding.legend.legendWaterUsageText;
        TextView legendAverageText = binding.legend.legendAverageText;
        TextView legendTargetText = binding.legend.legendTargetText;
        legendWaterUsageText.setText(dataLabel);
        legendAverageText.setText(avgLabel);
        legendTargetText.setText(targetLabel);
        View legendWaterUsageIcon = binding.legend.legendWaterUsageIcon;
        View legendAverageIcon = binding.legend.legendAverageIcon;
        View legendTargetIcon = binding.legend.legendTargetIcon;
        legendWaterUsageIcon.setBackgroundColor(dataColor);
        legendAverageIcon.setBackgroundColor(avgColor);
        legendTargetIcon.setBackgroundColor(targetColor);
    }

    public List<BarEntry> convertDailyUsageToBarEntries(List<DailyUsage> dailyUsageList) {
        if (dailyUsageList == null || dailyUsageList.isEmpty()) {
            return new ArrayList<>();
        }

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

    private String formatDateLabel(String date) {
        String[] parts = date.split("-");
        if (parts.length != 3) return date;
        int month = Integer.parseInt(parts[1]);
        int day = Integer.parseInt(parts[2]);
        return months[month - 1] + " " + day;
    }

    private String formatMonthLabel(String date) {
        String[] parts = date.split("-");
        if (parts.length != 3) return date;
        int month = Integer.parseInt(parts[1]);
        return months[month - 1];
    }

    private String formatYearLabel(String date) {
        String[] parts = date.split("-");
        if (parts.length != 3) return date;
        return parts[0];
    }

    public List<DailyUsage> getSublistByViewRange(List<DailyUsage> dailyUsageList, String[] dataRange) {
        List<DailyUsage> result = new ArrayList<>();
        if (dataRange == null || dataRange.length < 2) return result;

        String startDate = dataRange[0];
        String endDate = dataRange[1];

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
            // still build zero entries across the date range
            Calendar iterCal = (Calendar) startCal.clone();
            while (!iterCal.after(endCal)) {
                result.add(new DailyUsage(sdf.format(iterCal.getTime()), 0f));
                iterCal.add(Calendar.DAY_OF_MONTH, 1);
            }
            // set stats to zero
            avg = 0f;
            setStatistics(0f, 0f, 0f);
            return result;
        }

        if ("year".equals(viewRange)) {
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
            setStatistics(low == Float.MAX_VALUE ? 0f : low, high == Float.MIN_VALUE ? 0f : high, newAvg);
            return result;
        }

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
        setStatistics(low == Float.MAX_VALUE ? 0f : low, high == Float.MIN_VALUE ? 0f : high, newAvg);

        Calendar iterCal = (Calendar) startCal.clone();
        while (!iterCal.after(endCal)) {
            String dateStr = sdf.format(iterCal.getTime());
            DailyUsage du = usageMap.get(dateStr);
            if (du != null) result.add(new DailyUsage(du));
            else result.add(new DailyUsage(dateStr, 0f));
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
                cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
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
        if (dailyUsageList == null || targetDay == null) return -1;
        for (int i = 0; i < dailyUsageList.size(); i++) {
            DailyUsage dailyUsage = dailyUsageList.get(i);
            if (targetDay.equals(dailyUsage.getDay())) return i;
        }
        return -1;
    }

    public void setLabelText(String s) {
        if (binding != null) binding.labelText.setText(s);
    }

    public void setStatistics(float low, float high, Float avgValue) {
        if (binding == null || binding.statistics == null) return;
        if (avgValue == null) avgValue = 0f;
        if (low == Float.MAX_VALUE) low = 0f;
        if (high == Float.MIN_VALUE) high = 0f;
        binding.statistics.low.setText("Low: " + String.format("%.2f", low));
        binding.statistics.high.setText("High: " + String.format("%.2f", high));
        binding.statistics.avg.setText("Avg: " + String.format("%.2f", avgValue));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
