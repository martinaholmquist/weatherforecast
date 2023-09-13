package com.weatherForeCast.weatherforecast.METmodels;

import java.util.ArrayList;
import java.util.Date;

// import com.fasterxml.jackson.databind.ObjectMapper; // version 2.11.1
// import com.fasterxml.jackson.annotation.JsonProperty; // version 2.11.1
/* ObjectMapper om = new ObjectMapper();
RootSMHI root = om.readValue(myJsonString, RootSMHI.class); */
public class Data{
    public Instant instant;
    public Next12Hours next_12_hours;
    public Next1Hours next_1_hours;
    public Next6Hours next_6_hours;
}


