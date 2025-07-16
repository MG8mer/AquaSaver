package com.example.aquasaver.ui.conservation_tips;

import com.example.aquasaver.model.Suggestions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ConservationTipsSeeder {

    public static List<Suggestions> getConservationTips(String userEmail) {
        List<Suggestions> suggestions = new ArrayList<>();
        suggestions.add(new Suggestions(userEmail, "Rainy", "Collect rainwater: Install a rain barrel to capture water from downspouts for garden irrigation or other outdoor uses."));
        suggestions.add(new Suggestions(userEmail, "Rainy", "Check for leaks: Even during rainy periods, ensure your indoor plumbing and outdoor spigots aren't leaking, as this wastes water that's already readily available."));
        suggestions.add(new Suggestions(userEmail, "Rainy", "Adjust irrigation: Turn off or significantly reduce automatic irrigation systems during and immediately after rainfall."));
        suggestions.add(new Suggestions(userEmail, "Rainy", "Direct downspouts: Guide downspouts to permeable areas like lawns or gardens instead of directly onto paved surfaces to encourage groundwater recharge."));
        suggestions.add(new Suggestions(userEmail, "Rainy", "Plant strategically: Consider planting rain gardens or native plants that thrive with natural rainfall and require less supplemental watering."));

        suggestions.add(new Suggestions(userEmail, "Sunny", "Water during cool hours: Water your lawn and plants in the early morning or late evening to minimize evaporation."));
        suggestions.add(new Suggestions(userEmail, "Sunny", "Deep and infrequent watering: Water deeply to encourage root growth, but less frequently to prevent overwatering and allow soil to retain moisture."));
        suggestions.add(new Suggestions(userEmail, "Sunny", "Mulch garden beds: Apply a layer of mulch around plants to reduce water evaporation from the soil."));
        suggestions.add(new Suggestions(userEmail, "Sunny", "Prioritize watering: Focus water on new plants or those that show signs of stress, rather than uniformly watering everything."));
        suggestions.add(new Suggestions(userEmail, "Sunny", "Use drip irrigation: Install drip irrigation or soaker hoses for targeted watering, minimizing water loss to evaporation and runoff."));

        suggestions.add(new Suggestions(userEmail, "Humid", "Reduce watering frequency: Humid conditions mean less evaporation from plants, so they may not need as much frequent watering. Check soil moisture before watering."));
        suggestions.add(new Suggestions(userEmail, "Humid", "Monitor for overwatering: High humidity can increase the risk of fungal diseases if plants are consistently overwatered. Allow soil to dry out slightly between waterings."));
        suggestions.add(new Suggestions(userEmail, "Humid", "Consider plant choices: Select plants that are well-suited to humid climates and naturally require less supplemental water."));
        suggestions.add(new Suggestions(userEmail, "Humid", "Check for condensation: In indoor settings, ensure air conditioning units are draining properly and not dripping excessively, as this indicates wasted water."));
        suggestions.add(new Suggestions(userEmail, "Humid", "Ventilate greenhouses: If you have a greenhouse, proper ventilation can help manage humidity and reduce the need for excessive watering."));

        suggestions.add(new Suggestions(userEmail, "General", "Fix leaks promptly: A constantly dripping faucet can waste thousands of gallons of water per year. Repair all leaks immediately."));
        suggestions.add(new Suggestions(userEmail, "General", "Take shorter showers: Aim for 5-minute showers instead of baths."));
        suggestions.add(new Suggestions(userEmail, "General", "Turn off the tap: Don't let the water run while brushing your teeth, shaving, or washing dishes."));
        suggestions.add(new Suggestions(userEmail, "General", "Full loads of laundry/dishwasher: Only run washing machines and dishwashers when they are full."));
        suggestions.add(new Suggestions(userEmail, "General", "Install water-efficient fixtures: Replace old toilets, showerheads, sprinklers, and faucets with low-flow models."));
        suggestions.add(new Suggestions(userEmail, "General", "Educate others: Share water conservation tips with friends, family, and your community."));
        return suggestions;
    }
}
