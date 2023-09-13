package com.weatherForeCast.weatherforecast.MainModule;

import com.weatherForeCast.weatherforecast.provider.WeatherProvider;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
public class ForeCast {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)  //auto eller uuid
    private UUID id;
    private LocalDate date;
    private LocalTime hour;
    private float temperature;

    //nya

    @GeneratedValue(strategy = GenerationType.AUTO)
    private LocalDateTime created;



    @Column(nullable = true)
    private float longitude;
    @Column(nullable = true)
    private float latitude;
    @Column(nullable = true)
    private boolean precipitation;


    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private WeatherProvider provider;



    public ForeCast(LocalDate date, LocalTime hour, float temperature, LocalDateTime created, float longitude, float latitude, boolean precipitation, WeatherProvider provider) {
        this.date = date;
        this.hour = hour;
        this.temperature = temperature;
        this.created = created;
        this.longitude = longitude;
        this.latitude = latitude;
        this.precipitation = precipitation;
        this.provider = provider;
    }



    //tom construktor
    public ForeCast() {
    }




    public WeatherProvider getProvider() {
        return provider;
    }
    public void setProvider(WeatherProvider provider) {
        this.provider = provider;
    }



    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public boolean isPrecipitation() {
        return precipitation;
    }

    public void setPrecipitation(boolean precipitation) {
        this.precipitation = precipitation;
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




    @Override
    public String toString() {
        return "ForeCast{" +
                "id=" + id +
                ", date=" + date +
                ", hour=" + hour +
                ", temperature=" + temperature +
                ", created=" + created +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", precipitation=" + precipitation +
                ", provider=" + provider +
                '}';
    }
}
