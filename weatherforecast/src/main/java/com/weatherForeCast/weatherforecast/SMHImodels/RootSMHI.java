package com.weatherForeCast.weatherforecast.SMHImodels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Date;

@Service
@JsonIgnoreProperties(ignoreUnknown = true)

public class RootSMHI {

    public Date approvedTime;
    public Geometry geometry;
    public ArrayList<TimeSeries> timeSeries;

    public Date getApprovedTime() {
        return approvedTime;
    }

    public void setApprovedTime(Date approvedTime) {
        this.approvedTime = approvedTime;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public ArrayList<TimeSeries> getTimeSeries() {
        return timeSeries;
    }

    public void setTimeSeries(ArrayList<TimeSeries> timeSeries) {
        this.timeSeries = timeSeries;
    }
}
