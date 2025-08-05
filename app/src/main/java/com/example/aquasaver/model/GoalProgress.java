package com.example.aquasaver.model;

import java.util.Date;

public class GoalProgress {
    private int id;
    private String userEmail;
    private int goalAmount;
    private double amountLogged;
    private Date progressDate;
    private boolean onTarget;

    public GoalProgress() {}

    public GoalProgress(String userEmail, double amountLogged, Date progressDate, boolean onTarget, int goalAmount) {
        this.userEmail = userEmail;
        this.amountLogged = amountLogged;
        this.progressDate = progressDate;
        this.onTarget = onTarget;
        this.goalAmount = goalAmount;
    }


    public int getId() { // Getter for id
        return id;
    }

    public String getUserEmail() { // Getter for userEmail
        return userEmail;
    }

    public void setUserEmail( String userEmail) { // Setter for userEmail
        this.userEmail = userEmail;
    }

    public double getAmountLogged() { // Getter for amountLogged
        return amountLogged;
    }

    public void setAmountLogged(double amountLogged) { // Setter for amountLogged
        this.amountLogged = amountLogged;
    }

    public Date getProgressDate() { // Getter for progressDate
        return progressDate;
    }

    public void setProgressDate(Date progressDate) { // Setter for progressDate
        this.progressDate = progressDate;
    }

    public boolean getOnTarget() { // Getter for onTarget
        return onTarget;
    }

    public void setOnTarget(boolean onTarget) { // Setter for onTarget
        this.onTarget = onTarget;
    }

    public int getGoalAmount()
    {
        return goalAmount;
    }

    public void setGoalAmount(int gA)
    {
        goalAmount = gA;
    }

}