package com.example.aquasaver.model;

import java.util.Date;

public class WeatherSuggestions {
    private int id;
    private String userEmail;
    private String location;
    private Date date;
    private String weatherReport;
    private String usageSuggestionText;
    private String rawWeatherDataJson;

    public WeatherSuggestions() {}

    public WeatherSuggestions(String userEmail, String location, Date date, String weatherReport, String usageSuggestionText) {
        this.userEmail = userEmail;
        this.location = location;
        this.date = date;
        this.weatherReport = weatherReport;
        this.usageSuggestionText = usageSuggestionText;
    }


    public int getId() { return id; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public String getWeatherReport() { return weatherReport; }
    public void setWeatherReport(String weatherReport) { this.weatherReport = weatherReport; }

    public String getUsageSuggestionText() { return usageSuggestionText; }
    public void setUsageSuggestionText(String usageSuggestionText) { this.usageSuggestionText = usageSuggestionText; }

    @Override
    public String toString() {
        return "WeatherSuggestions{" +
                "email='" + userEmail + '\'' +
                ", location='" + location + '\'' +
                ", date=" + date +
                ", suggestionText='" + usageSuggestionText + '\'' +
                '}';
    }
}