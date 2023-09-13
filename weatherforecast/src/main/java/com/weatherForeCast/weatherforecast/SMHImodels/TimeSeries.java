package com.weatherForeCast.weatherforecast.SMHImodels;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Date;
@Service
@JsonIgnoreProperties(ignoreUnknown = true)

public class TimeSeries {

    public Date validTime;
    public ArrayList<Parameter> parameters;

    public Date getValidTime() {
        return validTime;
    }

    public void setValidTime(Date validTime) {
        this.validTime = validTime;
    }

    public ArrayList<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(ArrayList<Parameter> parameters) {
        this.parameters = parameters;
    }
}
