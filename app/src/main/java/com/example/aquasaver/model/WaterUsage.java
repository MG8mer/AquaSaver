package com.example.aquasaver.model;

import java.util.Date;

public class WaterUsage {
    private int id;
    private String userEmail;
    private Date usageDate;
    private double amount;
    private String activityType;
    private String units;

    public WaterUsage() {}

    public WaterUsage(String userEmail, Date usageDate, double amount, String activityType, String units) {
        this.userEmail = userEmail;
        this.usageDate = usageDate;
        this.amount = amount;
        this.activityType = activityType;
        this.units = units;
    }

    public int getId() { // Getter for id
        return id;
    }

    public String getUserEmail() { // Getter for userEmail
        return userEmail;
    }

    public void setUserEmail(String userEmail) { // Setter for userEmail
        this.userEmail = userEmail;
    }

    public Date getUsageDate() { // Getter for usageDate
        return usageDate;
    }

    public void setUsageDate(Date usageDate) { // Setter for usageDate
        this.usageDate = usageDate;
    }

    public double getAmount() { // Getter for amountLiters
        return amount;
    }

    public void setAmount(double amount) { // Setter for amountLiters
        this.amount = this.amount;
    }

    public String getActivityType() { // Getter for activityType
        return activityType;
    }

    public void setActivityType(String activityType) { // Setter for activityType
        this.activityType = activityType;
    }

    public String getUnits()
    {
        return units;
    }

    public void setUnits(String units)
    {
        this.units = units;
    }
}