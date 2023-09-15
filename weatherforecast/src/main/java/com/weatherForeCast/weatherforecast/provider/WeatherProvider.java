package com.weatherForeCast.weatherforecast.provider;


public enum WeatherProvider {

    SMHI(),
    MET(),
    USER()


    /* här använder jag int
    SMHI(1),
    MET(2),
    USER(3);

    private final int value;  //ska ej ändras

    WeatherProvider(int value) {  //konstruktorn
        this.value = value;
    }

    public int getValue() { //här hämtas värdet som typas in som provider
        // provider.getValue(), returnerar inten som den tillhör.
        return value;
    }

    public static WeatherProvider fromValue(int value) {
        for (WeatherProvider provider : WeatherProvider.values()) {
            if (provider.value == value) {
                return provider;
            }
        }
        throw new IllegalArgumentException("Invalid WeatherProvider value: " + value);
    }*/
}
