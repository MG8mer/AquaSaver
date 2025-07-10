package com.example.aquasaver.ui.water_usage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.aquasaver.databinding.FragmentGoalsBinding;
import com.example.aquasaver.databinding.FragmentWaterUsageBinding;
import com.example.aquasaver.databinding.WaterGraphBinding;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WaterUsageFragment extends Fragment {

    private FragmentWaterUsageBinding binding;
    private WaterGraphBinding barChart;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        WaterUsageViewModel waterusageViewModel =
                new ViewModelProvider(this).get(WaterUsageViewModel.class);

        binding = FragmentWaterUsageBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        barChart = binding.waterGraph;
        BarChart barChart1 = barChart.waterGraph;
        BarDataSet barDataSet = new BarDataSet(dataValues1(), "Water Usage");
        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(barDataSet);

        BarData data = new BarData(dataSets);
        barChart1.setData(data);
        barChart1.getDescription().setEnabled(false);
        barChart1.animateY(2000);
        barChart1.invalidate();



        //final TextView textView = binding.textWaterUsage;
        //waterusageViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;


    }

    private ArrayList<BarEntry> dataValues1() {
        ArrayList<BarEntry> dataValues = new ArrayList<>();
        dataValues.add(new BarEntry(1, 10));
        dataValues.add(new BarEntry(2, 20));
        dataValues.add(new BarEntry(3, 30));
        return dataValues;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}