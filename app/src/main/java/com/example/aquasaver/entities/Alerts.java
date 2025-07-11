package com.example.aquasaver.entities;

// Import important room libraries
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.aquasaver.entities.enums.AlertType;

// Enum data type for weather alert variants (heatwaves, drought, etc.)

@Entity(tableName = "alerts")

public class Alerts
{
    @PrimaryKey(autoGenerate = true)
    public int id;

    // Define instance variables for each column
    @ColumnInfo(name = "location")
    public String location; // User location

    @ColumnInfo(name = "alert_type")
    public AlertType alertType; // Type of alert

    @ColumnInfo(name = "description")
    public String description; // Alert description

    @ColumnInfo(name = "severity")
    public String severity; // Alert severity
    @ColumnInfo(name = "start_time")
    public Long startTime; // Start time of an alert

    @ColumnInfo(name = "end_time")
    public Long endTime; // End time of an alert

    // Constructor
    public Alerts(String location, AlertType alertType, String description, String severity, Long startTime, Long endTime)
    {
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
