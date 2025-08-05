package com.example.aquasaver.model;

import com.example.aquasaver.model.enums.SummaryType;
import java.util.Date;

public class Reports {
    private int id;
    private String userEmail;
    private SummaryType summaryType;
    private Date startDate;
    private Date endDate;
    private int totalLitersUsed;
    private int challengesCompleted;
    private int streakCount;

    public Reports() {}

    public Reports(String userEmail, SummaryType summaryType, Date startDate, Date endDate,
                   int totalLitersUsed, int challengesCompleted, int streakCount) {
        this.userEmail = userEmail;
        this.summaryType = summaryType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalLitersUsed = totalLitersUsed;
        this.challengesCompleted = challengesCompleted;
        this.streakCount = streakCount;
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

    public int getTotalLitersUsed() { return totalLitersUsed; }
    public void setTotalLitersUsed(int totalLitersUsed) { this.totalLitersUsed = totalLitersUsed; }

    public int getChallengesCompleted() { return challengesCompleted; }
    public void setChallengesCompleted(int challengesCompleted) { this.challengesCompleted = challengesCompleted; }

    public int getStreakCount() { return streakCount; }
    public void setStreakCount(int streakCount) { this.streakCount = streakCount; }
}
