package com.weatherForeCast.weatherforecast.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.Map;


public class AverageDTO {

    private LocalDate date;
    private float averageTemperature;
    private LocalTime hour;

    public AverageDTO(LocalDate date, float averageTemperature, LocalTime hour) {
        this.date = date;
        this.averageTemperature = averageTemperature;
        this.hour = hour;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public float getAverageTemperature() {
        return averageTemperature;
    }

    public void setAverageTemperature(float averageTemperature) {
        this.averageTemperature = averageTemperature;
    }

    public LocalTime getHour() {
        return hour;
    }

    public void setHour(LocalTime hour) {
        this.hour = hour;
    }


}
