package com.example.aquasaver.model;

import java.util.Date;

public class WaterUsage {
    private int id;
    private String userEmail;
    private Date usageDate;
    private double amountLiters;
    private String activityType;

    public WaterUsage() {}

    public WaterUsage(String userEmail, Date usageDate, double amountLiters, String activityType) {
        this.userEmail = userEmail;
        this.usageDate = usageDate;
        this.amountLiters = amountLiters;
        this.activityType = activityType;
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

    public double getAmountLiters() { // Getter for amountLiters
        return amountLiters;
    }

    public void setAmountLiters(double amountLiters) { // Setter for amountLiters
        this.amountLiters = amountLiters;
    }

    public String getActivityType() { // Getter for activityType
        return activityType;
    }

    public void setActivityType(String activityType) { // Setter for activityType
        this.activityType = activityType;
    }
}