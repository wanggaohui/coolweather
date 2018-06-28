package com.example.acer.coolweather.goson;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Weather {
    public String status;
    public AQI aqi;
    public Basic basic;
    public Now now;
    public Suggestion suggestion;
    @SerializedName("daily_forecast")
    public List<Forecast> forecastList = new ArrayList<>();
}
