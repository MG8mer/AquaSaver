package com.example.aquasaver.ui.conservation_tips;

import com.example.aquasaver.model.Suggestions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ConservationTipsSeeder {

    public static List<Suggestions> getConservationTips(String userEmail) {
        List<Suggestions> suggestions = new ArrayList<>();

        suggestions.add(new Suggestions(userEmail, "Rainy", "Collect Rainwater", "Install a rain barrel to capture water from downspouts for garden irrigation or other outdoor uses."));
        suggestions.add(new Suggestions(userEmail, "Rainy", "Check for Leaks", "Ensure your indoor plumbing and outdoor spigots aren't leaking, as this wastes water."));
        suggestions.add(new Suggestions(userEmail, "Rainy", "Adjust Irrigation", "Turn off or reduce automatic irrigation systems during and after rainfall."));
        suggestions.add(new Suggestions(userEmail, "Rainy", "Direct Downspouts", "Guide downspouts to permeable areas to encourage groundwater recharge."));
        suggestions.add(new Suggestions(userEmail, "Rainy", "Plant Strategically", "Plant rain gardens or native plants that thrive with natural rainfall."));

        suggestions.add(new Suggestions(userEmail, "Sunny", "Water During Cool Hours", "Water your lawn and plants in early morning or late evening to minimize evaporation."));
        suggestions.add(new Suggestions(userEmail, "Sunny", "Deep and Infrequent Watering", "Water deeply to encourage root growth but less frequently."));
        suggestions.add(new Suggestions(userEmail, "Sunny", "Mulch Garden Beds", "Apply mulch to reduce water evaporation from the soil."));
        suggestions.add(new Suggestions(userEmail, "Sunny", "Prioritize Watering", "Focus water on new plants or those showing stress."));
        suggestions.add(new Suggestions(userEmail, "Sunny", "Use Drip Irrigation", "Install drip irrigation for targeted watering."));

        suggestions.add(new Suggestions(userEmail, "Humid", "Reduce Watering Frequency", "Water less frequently, check soil moisture first."));
        suggestions.add(new Suggestions(userEmail, "Humid", "Monitor for Overwatering", "Avoid fungal diseases by letting soil dry out."));
        suggestions.add(new Suggestions(userEmail, "Humid", "Consider Plant Choices", "Select plants suited to humid climates."));
        suggestions.add(new Suggestions(userEmail, "Humid", "Check for Condensation", "Ensure AC units drain properly to avoid water waste."));
        suggestions.add(new Suggestions(userEmail, "Humid", "Ventilate Greenhouses", "Manage humidity to reduce excess watering."));

        suggestions.add(new Suggestions(userEmail, "General", "Fix Leaks Promptly", "Repair dripping faucets immediately."));
        suggestions.add(new Suggestions(userEmail, "General", "Take Shorter Showers", "Aim for 5-minute showers instead of baths."));
        suggestions.add(new Suggestions(userEmail, "General", "Turn Off Tap", "Don't let water run while brushing teeth or washing dishes."));
        suggestions.add(new Suggestions(userEmail, "General", "Full Loads Only", "Run washing machines and dishwashers only when full."));
        suggestions.add(new Suggestions(userEmail, "General", "Install Water-Efficient Fixtures", "Replace old fixtures with low-flow models."));
        suggestions.add(new Suggestions(userEmail, "General", "Educate Others", "Share water conservation tips with your community."));

        return suggestions;
    }
}
