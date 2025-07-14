package com.example.aquasaver.ui.water_usage;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.renderer.YAxisRenderer;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.android.material.button.MaterialButtonToggleGroup;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.aquasaver.databinding.FragmentWaterUsageBinding;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.example.aquasaver.R;

public class WaterUsageFragment extends Fragment {

    DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

    // Format current date and time
    String currentDateString = dateFormat.format(new Date());
    String[] date = currentDateString.split("-");
    public int day = Integer.parseInt(date[0]);
    public int month = Integer.parseInt(date[1]);
    public int year = Integer.parseInt(date[2]);

    public int daysInFebruary = year % 4 == 0 ? 29 : 28;
    public int[] daysInMonth = {31, daysInFebruary, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    public static String[] days = {
            "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
    };

    public static String[] today = {
            "Today"
    };

    public static String[] months = {
            "Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    private String[] label;
    private FragmentWaterUsageBinding binding;
    private BarChart barChart;

    private final List<BarEntry> yeardata = new ArrayList<>();

    private final List<BarEntry> dataShowing = new ArrayList<>();

    private int[] indexBounds;

    private float dailyTarget = 30f;
    private float avg;

    String dataToShow;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        WaterUsageViewModel waterusageViewModel =
                new ViewModelProvider(this).get(WaterUsageViewModel.class);

        binding = FragmentWaterUsageBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Log.d("DATE", currentDateString);


        addEmptyYear();

        binding.graphToggle.graphToggle.check(R.id.week);
        dataToShow = "week";
        label = days;
        setDataShowing();
        generateGraph();

//        binding.addDay.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                addDay(10);
//                setDataShowing(dataToShow);
//                generateGraph();
//                Log.d("YEARDATA", yeardata.toString());
//            }
//        });

        binding.graphToggle.graphToggle.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {


                if (isChecked) {
                    label = new String[]{};
                    if (checkedId == R.id.week) {
                        Log.d("GRAPH TOGGLE", "WEEK");
                        dataToShow = "week";
                        setDataShowing();
                        label = days;
                    } else if (checkedId == R.id.month) {
                        Log.d("GRAPH TOGGLE", "MONTH");
                        dataToShow = "month";
                        setDataShowing();
                        //String[] arr = new String[]{months[month]};
                        String[] arr = new String[] {"1", "8", "15", "22", "29"};
                        label = arr;
                    } else if (checkedId == R.id.year) {
                        Log.d("GRAPH TOGGLE", "YEAR");
                        dataToShow = "year";
                        setDataShowing();
                        label = months;
                    } else if (checkedId == R.id.day) {
                        Log.d("GRAPH TOGGLE", "DAY");
                        dataToShow = "day";
                        setDataShowing();
                        label = today;
                    }
                    Log.d("YEARDATA", dataToShow.toString());
                    generateGraph();
                }
            }
        });


        return root;


    }

    private void addEmptyYear() {
        for (int i = 0; i < 194; i++) {
            yeardata.add(new BarEntry(yeardata.size(), i));
        }
        if (year % 4 == 0) {
            yeardata.add(new BarEntry(yeardata.size(), 0));
        }
        Log.d("YEARDATA", "yeardata: " + yeardata.toString());
    }


    public void setDataShowing() {
        //find current day (1-365/366)
        int curDay = 0;
        for (int i = 0; i < month - 1; i++) {
            curDay += daysInMonth[i];
        }
        curDay += day;

        //find start of week (sunday)
        int dayOfWeek = getDayOfWeek(year, month, day);
        Log.d("YEARDATA", "dayOfWeek: " + dayOfWeek);
        int sunday = curDay - (dayOfWeek - 1);

        //find start of month
        int theFirst = curDay - day + 1;

        dataShowing.clear();
        int yeardataSize = year % 4 == 0 ? 366 : 365;
        List<BarEntry> yeardataCopy = new ArrayList<>(yeardata);
        for (int i = 0; i < yeardataSize; i++) {
            if (i < yeardata.size()) {
                if (yeardata.get(i) == null) {
                    yeardataCopy.set(i, new BarEntry(i, 0));
                }
            } else {
                yeardataCopy.add(new BarEntry(i, 0));
            }
        }

        if (dataToShow == "day") {
            indexBounds = new int[]{curDay - 1, curDay};

        } else if (dataToShow == "week") {
            indexBounds = new int[]{sunday - 1, sunday + 6};

        } else if (dataToShow == "month") {
            indexBounds = new int[]{theFirst - 1, theFirst + daysInMonth[month - 1]};

        } else if (dataToShow == "year") {
            //convert bar entries from days to months
            for (int m : daysInMonth) {
                int sum = 0;
                for (int i = 0; i < m; i++) {
                    sum += (int) yeardataCopy.get(i).getY();
                }
                for (int i = 0; i < m; i++) {
                    yeardataCopy.remove(0);
                }
                dataShowing.add(new BarEntry(dataShowing.size(), sum));
            }

        }

        if (dataToShow != "year") {
            for (int i = indexBounds[0]; i < indexBounds[1]; i++) {
                float y = yeardataCopy.get(i).getY();
                dataShowing.add(new BarEntry(dataShowing.size(), y)); // new x = 0
            }

        }

        for(int i = 0; i < dataShowing.size(); i++){
            dataShowing.get(i).setX(i);
        }
    }

    public void addDay(int y) {
        yeardata.add(new BarEntry(yeardata.size(), y));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public static int getDayOfWeek(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day); // Month is 0-based (January = 0)

        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK); // Sunday = 1, Saturday = 7

        return dayOfWeek;
    }

    public void generateGraph() {

        int barColor = getThemeAttribute(getContext(), R.attr.colorPrimary).data;
        int targetColor = Color.GREEN;
        int avgColor = Color.argb(128, 0, 0, 0);

        //initialize and add data
        BarChart barChart = binding.waterGraph;
        BarDataSet barDataSet = new BarDataSet(dataShowing, "Water Usage");
        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(barDataSet);

        BarData data = new BarData(dataSets);
        barChart.setData(data);

        // Calculate average
        float sum = 0;
        for (BarEntry entry : dataShowing) {
            sum += entry.getY();
        }
        float average = dataShowing.size() > 0 ? sum / dataShowing.size() : 0;

        // <editor-fold desc="create avg and target line">
        LimitLine avgLine = new LimitLine(average, "");
        avgLine.setLineColor(avgColor);
        avgLine.setLineWidth(2f);
        avgLine.enableDashedLine(15f, 10f, 0f);
        //create target line
        LimitLine targetLine = new LimitLine((dataToShow == "year") ? dailyTarget*daysInMonth[month] : dailyTarget, "");
        targetLine.setLineColor(targetColor);
        targetLine.setLineWidth(2);
        targetLine.enableDashedLine(15f, 10f, 0f);
        // </editor-fold>

        // Set bar color
        barDataSet.setColor(barColor);

        // Add both lines to the left axis
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.removeAllLimitLines(); // clear old lines
        leftAxis.addLimitLine(avgLine);
        leftAxis.addLimitLine(targetLine);


        // <editor-fold desc="Create legend">

        Legend legend = barChart.getLegend();

        // Get current legend entries from data sets
        List<LegendEntry> originalEntries = new ArrayList<>();
        if (barChart.getData() != null && barChart.getData().getDataSets() != null) {
            for (IBarDataSet set : barChart.getData().getDataSets()) {
                LegendEntry entry = new LegendEntry();
                entry.label = set.getLabel();
                entry.formColor = set.getColor();
                entry.form = Legend.LegendForm.SQUARE;  // or LINE if you prefer
                originalEntries.add(entry);
            }
        }

        LegendEntry avgEntry = new LegendEntry();

        avgEntry.label = "Avg";
        avgEntry.formColor = avgColor;
        avgEntry.form = Legend.LegendForm.LINE;

        LegendEntry targetEntry = new LegendEntry();
        targetEntry.label = "Target";
        targetEntry.formColor = targetColor;
        targetEntry.form = Legend.LegendForm.LINE;

        // Combine original and custom entries
        List<LegendEntry> combinedEntries = new ArrayList<>(originalEntries);
        combinedEntries.add(avgEntry);
        combinedEntries.add(targetEntry);

        // Set combined legend entries
        legend.setCustom(combinedEntries.toArray(new LegendEntry[0]));
        legend.setWordWrapEnabled(true);
        legend.setEnabled(true);
        legend.setXEntrySpace(20f);
       // </editor-fold>

        //no need for negative y values
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getAxisRight().setAxisMinimum(0f);


        XAxis xAxis = barChart.getXAxis();

        //<editor-fold desc="setting x axis labels">
        barDataSet.setDrawValues(false);
        if(dataToShow == "month") {
            xAxis.setLabelCount(daysInMonth[month - 1], true); // total days in this month

            int[] daysToLabel = new int[] {1, 8, 15, 22, 29};

            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    int day = (int) value + 1; // zero-based x to 1-based day

                    for (int d : daysToLabel) {
                        if (day == d) {
                            return String.valueOf(d);
                        }
                    }
                    return ""; // blank for other days
                }
            });
        } else {
            Log.d("YEARDATA", "week reached");
            xAxis.setLabelCount(100, false); // total days in this month
            Log.d("YEARDATA", "label: " + label.toString());
            xAxis.setValueFormatter(new IndexAxisValueFormatter(label));
        }


        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelRotationAngle(-45f);// one label per value
// </editor-fold>

        float maxValue = 0f;
        for (BarEntry entry : dataShowing) {
            if (entry.getY() > maxValue) {
                maxValue = entry.getY();
            }
        }
        leftAxis.setAxisMaximum(maxValue + 10); //
        barChart.getAxisRight().setAxisMaximum(maxValue + 10);
        //grid and axis settings
        barChart.getAxisRight().setDrawLabels(true);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        barChart.getDescription().setEnabled(false);
        barChart.animateY(500);
        barChart.setExtraBottomOffset(16f);

        barChart.invalidate();
    }

    public static TypedValue getThemeAttribute(Context context, @AttrRes int attr) {
        TypedValue typedValue = new TypedValue();
        if (context.getTheme().resolveAttribute(attr, typedValue, true)) {
            return typedValue;
        }
        return null;
    }


}