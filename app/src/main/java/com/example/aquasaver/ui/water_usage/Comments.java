//package com.example.aquasaver.ui.water_usage;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.graphics.Color;
//import android.os.Bundle;
//import android.util.Log;
//import android.util.TypedValue;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.lifecycle.ViewModelProvider;
//
//import com.example.aquasaver.R;
//import com.example.aquasaver.dao.GoalProgressDao;
//import com.example.aquasaver.dao.UserProfileDao;
//import com.example.aquasaver.dao.WaterUsageDao;
//import com.example.aquasaver.databinding.FragmentWaterUsageBinding;
//import com.example.aquasaver.db.AppDatabase;
//import com.example.aquasaver.model.GoalProgress;
//import com.example.aquasaver.model.UserProfile;
//import com.github.mikephil.charting.charts.BarChart;
//import com.github.mikephil.charting.components.Legend;
//import com.github.mikephil.charting.components.LegendEntry;
//import com.github.mikephil.charting.components.LimitLine;
//import com.github.mikephil.charting.components.XAxis;
//import com.github.mikephil.charting.components.YAxis;
//import com.github.mikephil.charting.data.BarData;
//import com.github.mikephil.charting.data.BarDataSet;
//import com.github.mikephil.charting.data.BarEntry;
//import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
//import com.google.android.material.button.MaterialButtonToggleGroup;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Calendar;
//import java.util.HashMap;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ThreadLocalRandom;
//
//public class Comments {
//
//    private Calendar calendar = Calendar.getInstance();
//    public int daysInFebruary = calendar.get(Calendar.YEAR) % 4 == 0 ? 29 : 28;
//    public int[] daysInMonth = {31, daysInFebruary, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
//
//    public static String[] daysInWeek = {
//            "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
//    };
//
//    private FragmentWaterUsageBinding binding;
//
//    public static String[] months = {
//            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
//
//    };
//
//    private final Map<Integer, List<BarEntry>> data = new LinkedHashMap<>();
//    private final List<BarEntry> thisYearsData = new ArrayList<>();
//
//    private BarChart barChart;
//
//    private String[] labels;
//
//    private float target = 100f;
//
//    private float avg;
//    private String timeWindow;
//
//    private int startDay;
//
//    private int[] lowDate;
//
//    private int[] highDate;
//
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             ViewGroup container, Bundle savedInstanceState) {
//        WaterUsageViewModel waterusageViewModel =
//                new ViewModelProvider(this).get(WaterUsageViewModel.class);
//
//        binding = FragmentWaterUsageBinding.inflate(inflater, container, false);
//        View root = binding.getRoot();
//
//        Context context = requireContext(); // or getContext(), if you know it's non-null
//        AppDatabase db = AppDatabase.getInstance(context);
//
//        WaterUsageDao waterUsageDao = db.waterUsageDao();
//        UserProfileDao userProfileDao = db.userProfileDao();
//        GoalProgressDao goalProgressDao = db.goalProgressDao();
//
//
//        new Thread(() -> {
//            SharedPreferences prefs = requireActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
//            String userEmail = prefs.getString("username", null);
//
//            if (userEmail == null) {
//                requireActivity().runOnUiThread(() ->
//                        Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_LONG).show());
//                return;
//            }
//
//            UserProfile user = userProfileDao.getUserByEmail(userEmail);
//
//            if (user == null) {
//                requireActivity().runOnUiThread(() ->
//                        Toast.makeText(requireContext(), "User not found. Please log in.", Toast.LENGTH_LONG).show());
//                return;
//            }
//
//            GoalProgress goalProgress = goalProgressDao.getTodayProgress(userEmail);
//            float targetValue = goalProgress.getGoalAmount();
//            target = targetValue;
//            float litersUsedToday = waterUsageDao.getLitersUsedToday(userEmail);
//            Log.d("DEBUG5", "litersUsedToday: " + litersUsedToday);
//
//            requireActivity().runOnUiThread(() -> {
//                Log.d("DEBUG5", "0");
//                if(thisYearsData.size() == 0) {
//                    addEmptyYear();
//                }
//                Log.d("DEBUG5", "1");
//                if(thisYearsData.size() < calendar.get(Calendar.DAY_OF_YEAR)) {
//                    addDay(calendar.get(Calendar.DAY_OF_YEAR)-1, litersUsedToday);
//                } else {
//                    thisYearsData.set(calendar.get(Calendar.DAY_OF_YEAR)-1, new BarEntry(calendar.get(Calendar.DAY_OF_YEAR)-1, litersUsedToday));
//                    data.put(calendar.get(Calendar.YEAR), thisYearsData);
//
//                }
//                Log.d("DEBUG5", "2");
//
//                lowDate = new int[]{calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.YEAR)};
//                highDate = new int[]{calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.YEAR)};
//                Log.d("DEBUG5", "3");
//
//                barChart = binding.waterGraph;
//
//                binding.graphToggle.check(R.id.week);
//                timeWindow = "week";
//                labels = daysInWeek;
//                Log.d("DEBUG5", "4");
//                setDateBounds();
//
//                generateGraph();
//            });
//
//
//
//        }).start();
//
//
//
//
//
//        binding.buttonBack.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                backPress();
//            }
//        });
//
//        binding.buttonForward.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                forwardPress();
//            }
//        });
//
//        binding.graphToggle.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
//            @Override
//            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
//
//                if (isChecked) {
//                    if (checkedId == R.id.week) {
//                        Log.d("GRAPH TOGGLE", "WEEK");
//                        timeWindow = "week";
//                        labels = daysInWeek;
//                    } else if (checkedId == R.id.month) {
//                        Log.d("GRAPH TOGGLE", "MONTH");
//                        timeWindow = "month";
//                        labels = new String[] {
//                                "1", "", "", "", "", "", "",
//                                "8", "", "", "", "", "", "",
//                                "15", "", "", "", "", "", "",
//                                "22", "", "", "", "", "", "",
//                                "29", "", ""
//
//                        };
//                    } else if (checkedId == R.id.year) {
//                        Log.d("GRAPH TOGGLE", "YEAR");
//                        labels = months;
//                        timeWindow = "year";
//                    } else if (checkedId == R.id.day) {
//                        Log.d("GRAPH TOGGLE", "DAY");
//                        timeWindow = "day";
//                        setDateBounds();
//                        labels = new String[] {months[lowDate[1]-1] + " " + lowDate[0]};
//                    }
//                    setDateBounds();
//
//                    Log.d("Date", "lowDate: " + Arrays.toString(lowDate));
//                    Log.d("Date", "highDate: " + Arrays.toString(highDate));
//                    generateGraph();
//                }
//            }
//        });
//
//        return root;
//
//
//    }
//
//    public void backPress() {
//        if(timeWindow == "week") {
//            changeLowDate(-7);
//            if(changeHighDate(-7)) {
//                changeLowDate(7);
//            }
//            String lowLabel = months[lowDate[1]-1] + " " + lowDate[0];
//            String highLabel = months[highDate[1]-1] + " " + highDate[0];
//
//            Log.d("dATE2", "lowDayOfYear: " + getDayOfYear(lowDate[0], lowDate[1]-1, lowDate[2]));
//            Log.d("dATE2", "highDayOfYear: " + getDayOfYear(highDate[0], highDate[1]-1, highDate[2]));
//            Log.d("dATE2", "calendarDayOfYear: " + calendar.get(Calendar.DAY_OF_YEAR));
//            if(getDayOfYear(lowDate[0], lowDate[1]-1, lowDate[2]) <= calendar.get(Calendar.DAY_OF_YEAR) && getDayOfYear(highDate[0], highDate[1]-1, highDate[2]) >= calendar.get(Calendar.DAY_OF_YEAR)) {
//                setLabelText("This Week");
//
//            } else {
//                setLabelText(lowLabel + " - " + highLabel);
//            }
//
//        } else if(timeWindow == "month") {
//            changeLowDate(-daysInMonth[(lowDate[1]+10) % 12]);
//            if(changeHighDate(-daysInMonth[(highDate[1]-1) % 12])) {
//                changeLowDate(daysInMonth[(lowDate[1]-1) % 12]);
//            }
//
//            setLabelText(months[lowDate[1]-1]);
//        } else if(timeWindow == "day") {
//            changeLowDate(-1);
//            if(changeHighDate(-1)) {
//                changeLowDate(1);
//            }
//
//            if(calendar.get(Calendar.DAY_OF_MONTH) ==  lowDate[0]){
//                setLabelText("Today");
//            } else {
//                setLabelText(months[lowDate[1] - 1] + " " + lowDate[0]);
//            }
//            labels = new String[] {months[lowDate[1] - 1] + " " + lowDate[0]};
//        } else if(timeWindow == "year") {
//            boolean previousYearLeap = (lowDate[2]-1) % 4 == 0;
//            boolean currentYearLeap = lowDate[2] % 4 == 0;
//            changeLowDate(previousYearLeap ? -366 : -365);
//            if(changeHighDate(currentYearLeap ? -366 : -365)) {
//                changeLowDate(previousYearLeap ? 366 : 365);
//            }
//            setLabelText(String.valueOf(lowDate[2]));
//        }
//        generateGraph();
//    }
//
//    public void forwardPress() {
//        if(timeWindow == "week") {
//            changeLowDate(7);
//            if(changeHighDate(7)) {
//                changeLowDate(-7);
//            }
//            String lowLabel = months[lowDate[1]-1] + " " + lowDate[0];
//            String highLabel = months[highDate[1]-1] + " " + highDate[0];
//            if(getDayOfYear(lowDate[0], lowDate[1]-1, lowDate[2]) <= calendar.get(Calendar.DAY_OF_YEAR) && getDayOfYear(highDate[0], highDate[1]-1, highDate[2]) >= calendar.get(Calendar.DAY_OF_YEAR)) {
//                setLabelText("This Week");
//
//            } else {
//                setLabelText(lowLabel + " - " + highLabel);
//            }
//        } else if(timeWindow == "month") {
//            changeLowDate(daysInMonth[(lowDate[1]-1) % 12]);
//            if (changeHighDate(daysInMonth[(highDate[1]) % 12]) ) {
//                changeLowDate(-daysInMonth[(lowDate[1]+10) % 12]);
//            }
//            setLabelText(months[lowDate[1]-1]);
//        } else if(timeWindow == "day") {
//            changeLowDate(1);
//            if(changeHighDate(1)) {
//                changeLowDate(-1);
//            }
//
//            if(calendar.get(Calendar.DAY_OF_MONTH) ==  lowDate[0]){
//                setLabelText("Today");
//                Log.d("Date", "today:");
//            } else {
//                setLabelText(months[lowDate[1] - 1] + " " + lowDate[0]);
//            }
//            labels = new String[] {months[lowDate[1] - 1] + " " + lowDate[0]};
//        } else if(timeWindow == "year") {
//            boolean nextYearLeap = (lowDate[2]+1) % 4 == 0;
//            boolean currentYearLeap = lowDate[2] % 4 == 0;
//            changeLowDate(currentYearLeap ? 366 : 365);
//            if(changeHighDate(nextYearLeap ? 366 : 365)) {
//                changeLowDate(currentYearLeap ? -366 : -365);
//            }
//
//            setLabelText(String.valueOf(lowDate[2]));
//        }
//        generateGraph();
//    }
//
//
//    public void changeLowDate(int change) {
//        lowDate[0] += change;
//        int[] newDaysInMonth = {31, lowDate[2] % 4 == 0 ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
//        if(lowDate[0] < 1) {
//            while(lowDate[0] < 1) {
//                newDaysInMonth = new int[] {31, lowDate[2] % 4 == 0 ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
//                int overflow = lowDate[0];
//                Log.d("Date", "overflow: " + overflow);
//                lowDate[1]--;
//                if (lowDate[1] < 1) {
//                    lowDate[2]--;
//                    lowDate[1] = 12;
//                }
//                lowDate[0] = newDaysInMonth[lowDate[1]-1] + overflow;
//            }
//            Log.d("Date", "lowDate: " + Arrays.toString(lowDate));
//        }
//        if(lowDate[0] > newDaysInMonth[lowDate[1]-1]) {
//            while(lowDate[0] > newDaysInMonth[lowDate[1]-1]) {
//                newDaysInMonth = new int[] {31, lowDate[2] % 4 == 0 ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
//                int overflow = lowDate[0] - newDaysInMonth[lowDate[1]-1];
//                lowDate[1]++;
//                if (lowDate[1] > 12) {
//                    lowDate[2]++;
//                    lowDate[1] = 1;
//                }
//                lowDate[0] = overflow + (overflow == 0 ? 1 : 0);
//            }
//        }
//        Log.d("DEBUG2", "lowDate: " + Arrays.toString(lowDate));
//    }
//
//    public boolean changeHighDate(int change) {
//        int[] highDateCopy = new int[]{0, 0, 0};
//        for(int i=0; i<highDate.length; i++) {
//            highDateCopy[i] = highDate[i];
//        }
//        highDate[0] += change;
//        int[] newDaysInMonth = {31, highDate[2] % 4 == 0 ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
//        if(highDate[0] < 1) {
//            while(highDate[0] < 1) {
//                newDaysInMonth = new int[] {31, highDate[2] % 4 == 0 ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
//                int overflow = highDate[0];
//                Log.d("Date", "Overflow: " + overflow);
//                highDate[1]--;
//                if (highDate[1] < 1) {
//                    highDate[2]--;
//                    highDate[1] = 12;
//                }
//                highDate[0] = newDaysInMonth[highDate[1]-1] + overflow;
//            }
//            Log.d("Date", "highDate: " + Arrays.toString(highDate));
//        }
//        if(highDate[0] > newDaysInMonth[highDate[1]-1]) {
//            while(highDate[0] > newDaysInMonth[highDate[1]-1]) {
//                newDaysInMonth = new int[] {31, highDate[2] % 4 == 0 ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
//                int overflow = highDate[0] - newDaysInMonth[highDate[1]-1];
//                highDate[1]++;
//                if (highDate[1] > 12) {
//                    highDate[2]++;
//                    highDate[1] = 1;
//                }
//                highDate[0] = overflow + (overflow == 0 ? 1 : 0);
//            }
//        }
//        Log.d("DEBUG2", "highDate: " + Arrays.toString(highDate));
//        List<BarEntry> shownYear = data.get(lowDate[2]);
//        if(shownYear != null) {
//            boolean highInShownYear = false;
//            for (BarEntry entry : shownYear) {
//                if (entry.getX() == getDayOfYear(highDate[0], highDate[1] - 1, highDate[2])) {
//                    highInShownYear = true;
//                }
//            }
//            boolean lowInShownYear = false;
//            for (BarEntry entry : shownYear) {
//                if (entry.getX() == getDayOfYear(lowDate[0], lowDate[1] - 1, lowDate[2])-1) {
//                    lowInShownYear = true;
//                }
//            }
//            if (!lowInShownYear && !highInShownYear) {
//                highDate = highDateCopy;
//                Log.d("DEBUG2", "UNDO");
//                return true;
//            } else {
//                return false;
//            }
//        } else {
//            highDate = highDateCopy;
//            return true;
//
//        }
//    }
//
//
//    public void setDateBounds() {
//        lowDate = new int[]{calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.YEAR)};
//        highDate = new int[]{calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.YEAR)};
//        if(timeWindow == "week") {
//            changeLowDate(Calendar.SUNDAY - calendar.get(Calendar.DAY_OF_WEEK));
//            changeHighDate(Calendar.SATURDAY - calendar.get(Calendar.DAY_OF_WEEK));
//            setLabelText("This Week");
//        } else if(timeWindow == "month") {
//            changeLowDate(1-calendar.get(Calendar.DAY_OF_MONTH));
//            changeHighDate(daysInMonth[calendar.get(Calendar.MONTH)]-calendar.get(Calendar.DAY_OF_MONTH));
//            setLabelText(months[lowDate[1]-1]);
//        } else if(timeWindow == "day") {
//            setLabelText("Today");
//        } else if(timeWindow == "year") {
//            setLabelText(String.valueOf(lowDate[2]));
//            int currentYear = lowDate[2];
//            lowDate = new int[] {1, 1, currentYear};
//            int daysInYear = currentYear % 4 == 0 ? 366 : 365;
//            highDate = new int[] {daysInYear, 12, currentYear};
//        }
//
//        Log.d("DEBUG", "SetDateBounds Success");
//    }
//    public List<BarEntry> getSubsetData() {
//        List<BarEntry> originalData = data.get(lowDate[2]);
//        List<BarEntry> dataCopy = new ArrayList<>();
//        List<BarEntry> subset = new ArrayList<>();
//        if (originalData != null) {
//            for (BarEntry entry : originalData) {
//                // Create a new BarEntry with the same x and y values
//                dataCopy.add(new BarEntry(entry.getX(), entry.getY()));
//            }
//        }
//
//        int lowIndex = getDayOfYear(lowDate[0], lowDate[1]-1, lowDate[2])-1;
//        int highIndex = getDayOfYear(highDate[0], highDate[1]-1, highDate[2]);
//        Log.d("DEBUG", "lowIndex: " + lowIndex);
//        Log.d("DEBUG", "highIndex: " + highIndex);
//        if(timeWindow != "year") {
//            // Pad with 0s if lowIndex is less than 0
//            for (int i = lowIndex; i < 0; i++) {
//                subset.add(new BarEntry(i, 0));
//            }
//
//            float max = 0;
//            float min = 1000000000;
//            float sum = 0;
//            int count = 0;
//            // Add valid entries from dataCopy (only within bounds)
//            int from = Math.max(lowIndex, 0);
//            int to = Math.min(highIndex, dataCopy.size()); // exclusive
//            for (int i = from; i < to; i++) {
//                BarEntry original = dataCopy.get(i);
//                subset.add(new BarEntry(original.getX(), original.getY()));
//                float y = original.getY();
//                sum += y;
//                count++;
//                if(y > max) {
//                    max = y;
//                }
//                if(y < min) {
//                    min = y;
//                }
//
//            }
//            avg = sum/count;
//            setStatistics(min, max);
//            // Pad with 0s if highIndex exceeds bounds
//            for (int i = dataCopy.size(); i < highIndex; i++) {
//                subset.add(new BarEntry(i, 0));
//            }
//        } else {
//            int[] newDaysInMonth = {31, lowDate[2] % 4 == 0 ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
//            float total = 0;
//            float max = 0;
//            float min = 1000000000;
//            Map<Integer, Integer> count = new HashMap<>();
//            for(int i=0; i<12; i++) {
//                float sum = 0;
//                for(int j=0; j<newDaysInMonth[i]; j++) {
//                    if(!dataCopy.isEmpty() && dataCopy.get(0) != null) {
//                        sum += dataCopy.get(0).getY();
//                        total+=dataCopy.get(0).getY();
//                        dataCopy.remove(0);
//                        count.put(i, i);
//                    }
//                }
//                if(sum > max) {
//                    max = sum;
//                }
//                if(sum < min && sum != 0) {
//                    min = sum;
//                }
//                subset.add(new BarEntry(i, sum));
//                Log.d("DEBUG", "subset: " + subset.toString());
//            }
//            avg = total/count.size();
//            setStatistics(min, max);
//        }
//
//        Log.d("DEBUG", "subset: " + subset.toString());
//
//
//        for (int i = 0; i < subset.size(); i++) {
//            subset.get(i).setX(i);
//        }
//
//        return subset;
//    }
//
//    private void generateGraph() {
//        barChart.clear();
//        barChart.getAxisLeft().removeAllLimitLines();
//        barChart.getAxisRight().removeAllLimitLines();
//
//        List<BarEntry> dataToUse = getSubsetData();
//
//        // 1. Resolve colorPrimary from current theme
//        TypedValue typedValue = new TypedValue();
//        Context context = requireContext();
//        context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
//        int colorPrimary = typedValue.data;
//
//        // 2. Create dataset
//        BarDataSet dataSet = new BarDataSet(dataToUse, "Water Usage");
//        dataSet.setColor(colorPrimary); // Apply theme color
//        dataSet.setValueTextColor(Color.BLACK);
//        dataSet.setValueTextSize(12f);
//
//        // 3. Set data to chart
//        BarData barData = new BarData(dataSet);
//        barChart.setData(barData);
//
//        // 4. Configure axes
//        YAxis leftAxis = barChart.getAxisLeft();
//        YAxis rightAxis = barChart.getAxisRight();
//        leftAxis.setAxisMinimum(0f);
//        rightAxis.setAxisMinimum(0f);
//        leftAxis.setAxisMaximum(Math.max(timeWindow != "year" ? target : (float) (target * (lowDate[2] % 4 == 0 ? 366.0 : 365.0) / 12.0), getStatisticsMax())+5);
//        rightAxis.setAxisMaximum(Math.max(timeWindow != "year" ? target : (float) (target * (lowDate[2] % 4 == 0 ? 366.0 : 365.0) / 12.0), getStatisticsMax())+5);
//
//        leftAxis.removeAllLimitLines();
//        rightAxis.removeAllLimitLines();
//
//        int targetColor = Color.argb(225, 0, 255, 0);
//
//        int avgColor = Color.argb(128, 0, 0, 0);
//        // 5. Add average and target limit lines
//        LimitLine avgLine = new LimitLine(avg, "");
//        avgLine.setLineColor(avgColor);
//        avgLine.setLineWidth(2f);
//        avgLine.enableDashedLine(10f, 10f, 0f);
//
//        LimitLine targetLine = new LimitLine(timeWindow == "year" ? (float) (target * (lowDate[2] % 4 == 0 ? 366 : 365) / 12.0) : target, "");
//        targetLine.setLineColor(targetColor);
//        targetLine.setLineWidth(2f);
//        targetLine.enableDashedLine(15f, 10f, 0f);
//
//        leftAxis.addLimitLine(avgLine);
//        leftAxis.addLimitLine(targetLine);
//
//        Log.d("DEBUG", "MADE IT");
//        // 6. Configure X-axis
//        XAxis xAxis = barChart.getXAxis();
//        xAxis.setDrawGridLines(false);
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setGranularity(1f);
//        xAxis.setLabelCount(100, false);
//        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
//
//        // 7. Enable both Y axes
//        leftAxis.setEnabled(true);
//        rightAxis.setEnabled(true);
//
//        //legend
//
//        Legend legend = barChart.getLegend();
//        legend.setEnabled(true);
//        legend.resetCustom();
//
//        LegendEntry dataSetEntry = new LegendEntry();
//        dataSetEntry.label = "Water Usage"; // same as your BarDataSet label
//        dataSetEntry.formColor = colorPrimary; // same as your bar color
//        dataSetEntry.form = Legend.LegendForm.SQUARE;
//
//        LegendEntry avgEntry = new LegendEntry();
//        avgEntry.label = "Avg";
//        avgEntry.formColor = avgColor;
//        avgEntry.form = Legend.LegendForm.LINE;
//
//        LegendEntry targetEntry = new LegendEntry();
//        targetEntry.label = "Target";
//        targetEntry.formColor = targetColor;
//        targetEntry.form = Legend.LegendForm.LINE;
//
//        legend.setCustom(Arrays.asList(dataSetEntry, avgEntry, targetEntry));
//        legend.setXEntrySpace(15f);
//
//
//        //hide graph description and add padding
//        barChart.getDescription().setEnabled(false);
//        barChart.setExtraBottomOffset(16f);
//        barData.setDrawValues(false);
//
//
//
//        // 8. Redraw chart
//        barChart.invalidate();
//    }
//
//
//    public int getDayOfYear(int day, int month, int year) {
//        Calendar newCalendar = Calendar.getInstance();
//        newCalendar.set(Calendar.YEAR, year);
//        newCalendar.set(Calendar.MONTH, month); // 0-based (Jan = 0)
//        newCalendar.set(Calendar.DAY_OF_MONTH, day);
//        return newCalendar.get(Calendar.DAY_OF_YEAR);
//    }
//
//
//    public int getMostRecentSunday(int dayOfYear, int year) {
//        Calendar newCalendar = Calendar.getInstance();
//        newCalendar.set(Calendar.YEAR, year);
//        newCalendar.set(Calendar.DAY_OF_YEAR, dayOfYear);
//
//        int currentDayOfWeek = newCalendar.get(Calendar.DAY_OF_WEEK); // Sunday = 1, Monday = 2, ..., Saturday = 7
//        int daysSinceSunday = currentDayOfWeek - Calendar.SUNDAY;  // days to subtract
//        newCalendar.add(Calendar.DAY_OF_YEAR, -daysSinceSunday);      // move back to Sunday
//
//        return newCalendar.get(Calendar.DAY_OF_YEAR)-1;
//    }
//
//
//    public void addEmptyYear() {
//        Log.d("YEARDATA", "calendar.get(Calendar.DAY_OF_YEAR): " + calendar.get(Calendar.DAY_OF_YEAR));
//        for(int i=0; i<calendar.get(Calendar.DAY_OF_YEAR)-1; i++) {
//            float maxRandom = (float) (target*1.25);
//            float minRandom = (float) (target*0.60);
//            float randomValue = (float) ThreadLocalRandom.current().nextDouble(minRandom, maxRandom);
//            addDay(i, randomValue);
//        }
//        Log.d("YEARDATA", "thisYearsData: " + thisYearsData.toString());
//    }
//    public void addDay(int day, float value) {
//        thisYearsData.add(new BarEntry(day, value));
//        data.put(calendar.get(Calendar.YEAR), thisYearsData);
//    }
//
//    public void setLabelText(String txt) {
//        binding.labelText.setText(txt);
//    }
//
//
//    public void setStatistics(float low, float high) {
//        TextView l = binding.statistics.low;
//        TextView h = binding.statistics.high;
//        TextView a = binding.statistics.avg;
//        a.setText("Average: " + (int)(avg*10.0)/10.0);
//        l.setText("Low: " + (int)(low*10.0)/10.0);
//        h.setText("High: " + (int)(high*10.0)/10.0);
//    }
//
//    public float getStatisticsMax() {
//        TextView t = binding.statistics.high;
//        String returnText = ((String) t.getText()).replace("High: ", "");
//        return Float.parseFloat(returnText);
//    }
//}
