package com.example.aquasaver.model;

import com.example.aquasaver.model.enums.ChallengeGoalType;
import java.util.Date;

public class Challenges {
    private int id;
    private String userEmail;
    private String title;
    private String description;
    private ChallengeGoalType goalType;
    private int goalAmount;
    private Date startDate;
    private Date endDate;

    public Challenges() {}

    public Challenges(String userEmail, String title, String description, ChallengeGoalType goalType,
                      int goalAmount, Date startDate, Date endDate) {
        this.userEmail = userEmail;
        this.title = title;
        this.description = description;
        this.goalType = goalType;
        this.goalAmount = goalAmount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and setters for each instance variable
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ChallengeGoalType getGoalType() { return goalType; }
    public void setGoalType(ChallengeGoalType goalType) { this.goalType = goalType; }

    public int getGoalAmount() { return goalAmount; }
    public void setGoalAmount(int goalAmount) { this.goalAmount = goalAmount; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
}
