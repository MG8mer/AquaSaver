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

public class WaterUsageFragment extends Fragment {

    private FragmentWaterUsageBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        WaterUsageViewModel waterusageViewModel =
                new ViewModelProvider(this).get(WaterUsageViewModel.class);

        binding = FragmentWaterUsageBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textWaterUsage;
        waterusageViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}