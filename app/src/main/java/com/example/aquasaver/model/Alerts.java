package com.example.aquasaver.model;

import com.example.aquasaver.model.enums.AlertType;

public class Alerts {
    private int id;
    private String location;
    private AlertType alertType;
    private String description;
    private String severity;
    private Long startTime;
    private Long endTime;

    public Alerts() {}

    public Alerts(String location, AlertType alertType, String description, String severity, Long startTime, Long endTime) {
        this.location = location;
        this.alertType = alertType;
        this.description = description;
        this.severity = severity;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters and setter methods
    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public AlertType getAlertType()
    {
        return alertType;
    }

    public void setAlertType(AlertType alertType)
    {
        this.alertType = alertType;
    }

    public String getDescription()
    {
        return location;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getSeverity()
    {
        return severity;
    }

    public void setSeverity(String severity)
    {
        this.severity = severity;
    }

    public Long getStartTime()
    {
        return startTime;
    }

    public void setStartTime(Long startTime)
    {
        this.startTime = startTime;
    }

    public Long getEndTime()
    {
        return endTime;
    }

    public void setEndTime(Long endTime)
    {
        this.endTime = endTime;
    }
}
