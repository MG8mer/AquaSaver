package com.example.aquasaver.model;

import com.example.aquasaver.model.enums.SummaryType;
import java.util.Date;

public class Reports {
    private int id;
    private String userEmail;
    private SummaryType summaryType;
    private Date startDate;
    private Date endDate;
    private int totalUsage;
    private String usageUnits;
    private int challengesCompleted;
    private int streakCount;

    public Reports() {}

    public Reports(String userEmail, SummaryType summaryType, Date startDate, Date endDate,
                   int totalUsage, int challengesCompleted, int streakCount, String usageUnits) {
        this.userEmail = userEmail;
        this.summaryType = summaryType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalUsage = totalUsage;
        this.challengesCompleted = challengesCompleted;
        this.streakCount = streakCount;
        this.usageUnits = usageUnits;
    }

    // Getters and setters for all instance variables (table columns)
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String email) { this.userEmail = email; }

    public SummaryType getSummaryType() { return summaryType; }
    public void setSummaryType(SummaryType summaryType) { this.summaryType = summaryType; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public int getTotalUsage() { return totalUsage; }
    public void setTotalUsage(int totalUsage) { this.totalUsage = totalUsage; }

    public int getChallengesCompleted() { return challengesCompleted; }
    public void setChallengesCompleted(int challengesCompleted) { this.challengesCompleted = challengesCompleted; }

    public int getStreakCount() { return streakCount; }
    public void setStreakCount(int streakCount) { this.streakCount = streakCount; }

    public String getUsageUnits() { return usageUnits; }

    public void setUsageUnits(String usageUnits) { this.usageUnits = usageUnits; }
}
