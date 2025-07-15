package com.example.aquasaver.challenges;

import com.example.aquasaver.model.Challenges;
import com.example.aquasaver.model.enums.ChallengeGoalType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ChallengeSeeder {

    public static List<Challenges> getDefaultChallenges(String userEmail) {
        List<Challenges> challenges = new ArrayList<>();
        Date today = new Date();
        long oneDay = 24 * 60 * 60 * 1000L;

        challenges.add(new Challenges(userEmail, "Turn Off the Tap", "Don’t let the tap run while brushing your teeth for 7 days.", ChallengeGoalType.DAILY, 7, today, new Date(today.getTime() + 7 * oneDay)));
        challenges.add(new Challenges(userEmail, "Fix the Drip", "Identify and fix a leaking faucet or toilet.", ChallengeGoalType.DAILY, 3, today, new Date(today.getTime() + 3 * oneDay)));
        challenges.add(new Challenges(userEmail, "Low-Flow Loyalty", "Switch to a low-flow showerhead and track savings over a week.", ChallengeGoalType.WEEKLY, 7, today, new Date(today.getTime() + 7 * oneDay)));
        challenges.add(new Challenges(userEmail, "Greywater Reuse", "Reuse leftover water (e.g., from rinsing veggies) to water plants.", ChallengeGoalType.DAILY, 5, today, new Date(today.getTime() + 5 * oneDay)));
        challenges.add(new Challenges(userEmail, "Water at Dawn", "Water your plants early in the morning to reduce evaporation.", ChallengeGoalType.DAILY, 5, today, new Date(today.getTime() + 5 * oneDay)));
        challenges.add(new Challenges(userEmail, "Mulch It Up", "Apply mulch to a garden bed to retain moisture.", ChallengeGoalType.DAILY, 2, today, new Date(today.getTime() + 2 * oneDay)));
        challenges.add(new Challenges(userEmail, "Rain Catcher", "Set up a rain barrel or container to collect and reuse rainwater.", ChallengeGoalType.DAILY, 1, today, new Date(today.getTime() + oneDay)));
        challenges.add(new Challenges(userEmail, "Efficient Sprinkler Head", "Install or use a water-efficient sprinkler head.", ChallengeGoalType.DAILY, 2, today, new Date(today.getTime() + 2 * oneDay)));
        challenges.add(new Challenges(userEmail, "Smart Sprinkler System", "Use or research a smart sprinkler system.", ChallengeGoalType.DAILY, 1, today, new Date(today.getTime() + oneDay)));
        challenges.add(new Challenges(userEmail, "5-Minute Shower Challenge", "Take a 5-minute (or less) shower every day for 7 days.", ChallengeGoalType.DAILY, 7, today, new Date(today.getTime() + 7 * oneDay)));

        return challenges;
    }
    public static List<Challenges> getRandomChallenges(String userEmail, int count) {
        List<Challenges> allChallenges = getDefaultChallenges(userEmail);
        Collections.shuffle(allChallenges);
        return allChallenges.subList(0, Math.min(count, allChallenges.size()));
    }

}
