package com.example.aquasaver.weatherapi;

import java.util.List;

public class WeatherResponse {
    public Main main;
    public List<Weather> weather;
    public String name;

    public class Main {
        public float temp;
        public String name;
    }

    public class Weather {
        public String description;
    }
}
