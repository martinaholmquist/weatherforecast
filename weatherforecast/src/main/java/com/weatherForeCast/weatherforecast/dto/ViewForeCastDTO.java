package com.weatherForeCast.weatherforecast.dto;

import com.weatherForeCast.weatherforecast.provider.WeatherProvider;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;


public class ViewForeCastDTO {  //DataTransferObject

    private UUID id;
    private LocalDate date;
    private LocalTime hour;
    private float temperature;
    private boolean precipitation;
    @Enumerated(EnumType.STRING)
    private WeatherProvider provider;


    public ViewForeCastDTO(UUID id, LocalDate date, LocalTime hour, float temperature, boolean precipitation, WeatherProvider provider) {
        this.id = id;
        this.date = date;
        this.hour = hour;
        this.temperature = temperature;
        this.precipitation = precipitation;
        this.provider = provider;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getHour() {
        return hour;
    }

    public void setHour(LocalTime hour) {
        this.hour = hour;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public boolean isPrecipitation() {
        return precipitation;
    }

    public void setPrecipitation(boolean precipitation) {
        this.precipitation = precipitation;
    }

    public WeatherProvider getProvider() {
        return provider;
    }

    public void setProvider(WeatherProvider provider) {
        this.provider = provider;
    }
}
