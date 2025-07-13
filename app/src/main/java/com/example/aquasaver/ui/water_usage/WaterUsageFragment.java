package com.example.aquasaver.ui.water_usage;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButtonToggleGroup;

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

    private FragmentWaterUsageBinding binding;
    private BarChart barChart;

    private final List<BarEntry> yeardata = new ArrayList<>();

    private final List<BarEntry> dataShowing = new ArrayList<>();


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



    DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

    // Format current date and time
    String currentDateString = dateFormat.format(new Date());

    String dataToShow;
    String[] date = currentDateString.split("-");
    public int day = Integer.parseInt(date[0]);
    public int month = Integer.parseInt(date[1]);
    public int year = Integer.parseInt(date[2]);

    public int daysInFebruary = year%4 == 0 ? 29 : 28;
    public int[] daysInMonth = {31, daysInFebruary, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};


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
        setDataShowing(dataToShow);
        generateGraph();

        binding.addDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDay(10);
                setDataShowing(dataToShow);
                generateGraph();
                Log.d("YEARDATA", yeardata.toString());
            }
        });

        binding.graphToggle.graphToggle.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {


                if (isChecked) {
                    if(checkedId == R.id.week) {
                        Log.d("GRAPH TOGGLE", "WEEK");
                        dataToShow = "week";
                        setDataShowing(dataToShow);
                        label=days;
                    } else if(checkedId == R.id.month) {
                        Log.d("GRAPH TOGGLE", "MONTH");
                        dataToShow = "month";
                        setDataShowing(dataToShow);
                        String[] arr = new String[] { months[month] };
                        label=arr;
                    } else if(checkedId == R.id.year) {
                        Log.d("GRAPH TOGGLE", "YEAR");
                        dataToShow = "year";
                        setDataShowing(dataToShow);
                        label=months;
                    } else if(checkedId == R.id.day) {
                        Log.d("GRAPH TOGGLE", "DAY");
                        dataToShow = "day";
                        setDataShowing(dataToShow);
                        label=today;
                    }
                    generateGraph();
                }
            }
        });



        return root;


    }

    private void addEmptyYear() {
        for (int i = 0; i < 365; i++) {
            yeardata.add(new BarEntry(yeardata.size(), i));
        }
        if(year%4 == 0) {
            yeardata.add(new BarEntry(yeardata.size(), 0));
        }
        Log.d("YEARDATA", "yeardata: " + yeardata.toString());
    }



    public void setDataShowing(String length) {
        int curDay = 0;
        for(int i = 0; i < month-1; i++) {
            curDay+=daysInMonth[i];
        }
        curDay+=day;

        int dayOfWeek = getDayOfWeek(year, month, day);
        Log.d("YEARDATA", "dayOfWeek: " + dayOfWeek);
        int sunday = curDay - (dayOfWeek - 1);

        int theFirst = curDay-day+1;

        List<BarEntry> subset = new ArrayList<>();
        dataShowing.clear();
        int yeardataSize = year%4 == 0 ? 366 : 365;
        List<BarEntry> yeardataCopy = new ArrayList<>(yeardata);
        if(yeardataCopy.size() < yeardataSize) {
            while(yeardataCopy.size() < yeardataSize) {
                yeardataCopy.add(new BarEntry(yeardataCopy.size(), 0));
            }
        }
        if(length == "day") {
            subset = yeardataCopy.subList(curDay-1, curDay);
        } else if(length == "week") {
            subset = yeardataCopy.subList(sunday-1, sunday+6);
        } else if(length == "month") {
            subset = yeardataCopy.subList(theFirst-1, theFirst+daysInMonth[month-1]);
        } else if(length == "year") {
            dataShowing.clear();
            subset = yeardataCopy;
            for(int m : daysInMonth) {
                int sum = 0;
                for(int i = 0; i < m; i++) {
                    sum+= (int) subset.get(i).getY();
                }
                for(int i = 0; i < m; i++) {
                    subset.remove(0);
                }

                dataShowing.add(new BarEntry(dataShowing.size(), sum));
            }

        }
        //normalize x values
        if(length != "year") {
            for (int i = 0; i < subset.size(); i++) {
                float y = subset.get(i).getY();
                dataShowing.add(new BarEntry(i, y)); // new x = i
            }
        }

        Log.d("YEARDATA", "dataShowing: " + dataShowing.toString());
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
        BarChart barChart = binding.waterGraph;
        BarDataSet barDataSet = new BarDataSet(dataShowing, "Water Usage");
        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(barDataSet);


        BarData data = new BarData(dataSets);
        barChart.setData(data);
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f); // one label per value
        xAxis.setValueFormatter(new IndexAxisValueFormatter(label));
        xAxis.setDrawLabels(dataShowing.size() <= 27);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        barChart.getDescription().setEnabled(false);
        barChart.animateY(1000);
        barChart.invalidate();
    }



}