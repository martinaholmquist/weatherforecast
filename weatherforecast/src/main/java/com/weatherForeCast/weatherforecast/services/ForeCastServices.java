package com.weatherForeCast.weatherforecast.services;
import com.weatherForeCast.weatherforecast.MainModule.ForeCast;
import com.weatherForeCast.weatherforecast.dto.AverageDTO;
import com.weatherForeCast.weatherforecast.dto.ViewForeCastDTO;
import com.weatherForeCast.weatherforecast.provider.WeatherProvider;
import com.weatherForeCast.weatherforecast.repository.ForecastRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
public class ForeCastServices {

    @Autowired  //containern gör en abstraction som gör att det new:ar per automatik   Lagt till denna för refaktor av classer
    private ForecastRepository forecastRepository;



    public void updateWithDTO(ForeCast foreCastFromUser) throws IOException {
        forecastRepository.save(foreCastFromUser);
    }


    //ANVÄNDS EJ
    public List<ForeCast> getForeCastsTest() throws IOException {
        return forecastRepository.findAll();
    }


    public List<ForeCast> getForeCasts() throws IOException {
        List<ForeCast> orderedForecasts = forecastRepository.findAllOrdered();
        return orderedForecasts;
    }

    public void updateForecastByDate(UUID forecastId, LocalDate newDate) {
        forecastRepository.updateDateById(forecastId, newDate);
    }

    public void updateForecastByTime(UUID forecastId, LocalTime newTime) {
        forecastRepository.updateTimeById(forecastId, newTime);
    }

    public void updateForecastByTemperature(UUID forecastId, float newTemperature) {
        forecastRepository.updateTemperatureById(forecastId, newTemperature);
    }


    public void deleteWeather(UUID id) {
        forecastRepository.deleteById(id);
    }

    public  ForeCast add(ForeCast foreCast) throws IOException {
        forecastRepository.save(foreCast);
        return foreCast;
    }

    public List<AverageDTO> getAverageByDateDTO(LocalDate selectedDateOfAverage) {
        List<ForeCast> averageForecast = AverageAllDataBase(selectedDateOfAverage);

        if (averageForecast.isEmpty()) {
            return Collections.emptyList();
        }

        List<AverageDTO> result = new ArrayList<>();
        List<LocalTime> checktHours = new ArrayList<>();

        for (ForeCast forecast : averageForecast) {
            LocalTime hour = forecast.getHour();

            // Kontrollera om timmen som avses redan är gjord
            if (!checktHours.contains(hour)) {
                float totalTemperature = 0;
                int count = 0;

                //Den startar en ny loop för att räkna alla förekomster av timmen i averageForecast, för att lägga på temp ocg göra average
                for (ForeCast forecastHour : averageForecast) {
                    if (forecastHour.getHour().equals(hour)) {
                        totalTemperature += forecastHour.getTemperature();
                        count++;
                    }
                }

                if (count > 0) {
                    float averageTemperature = totalTemperature / count;
                    result.add(new AverageDTO(selectedDateOfAverage, averageTemperature, hour));
                }


                checktHours.add(hour);
            }
        }

        result.sort(Comparator.comparing(AverageDTO::getHour));

        return result;
    }

    public List<AverageDTO> getAverageFromWhatEverIsTypedInDTO(LocalDate selectedDateOfAverage, WeatherProvider provider) {
        List<ForeCast> averageForecast = getAverageByProvider(selectedDateOfAverage, provider);

        if (averageForecast.isEmpty()) {
            return Collections.emptyList();
        }

        List<AverageDTO> result = new ArrayList<>();
        List<LocalTime> checktHours = new ArrayList<>();

        for (ForeCast forecast : averageForecast) {
            LocalTime hour = forecast.getHour();

            // Check if this hour has already been processed
            if (!checktHours.contains(hour)) {
                float totalTemperature = 0;
                int count = 0;

                //Den startar en ny loop för att räkna alla förekomster av timmen i averageForecast, för att lägga på temp ocg göra average
                for (ForeCast forecastHour : averageForecast) {
                    if (forecastHour.getHour().equals(hour)) {
                        totalTemperature += forecastHour.getTemperature();
                        count++;
                    }
                }

                if (count > 0) {
                    float averageTemperature = totalTemperature / count;
                    result.add(new AverageDTO(selectedDateOfAverage, averageTemperature, hour));
                }


                checktHours.add(hour);
            }
        }

        result.sort(Comparator.comparing(AverageDTO::getHour));

        return result;
    }

    public List<ViewForeCastDTO> listAllForeCastFromDBWithDTO() throws IOException {

        List<ForeCast> allForeCastsInDB = getForeCasts();


        if (allForeCastsInDB.isEmpty()) {
            return Collections.emptyList();
        }

        List<ViewForeCastDTO> result = new ArrayList<>();


        for (ForeCast forecast : allForeCastsInDB) {
            LocalTime hour = forecast.getHour();
            LocalDate date = forecast.getDate();
            float temperature = forecast.getTemperature();
            boolean precipitation = forecast.isPrecipitation();
            WeatherProvider provider = forecast.getProvider();
            result.add(new ViewForeCastDTO(date, hour, temperature, precipitation,provider ));
        }
        return result;
    }

    public List<ForeCast> getAverageByProvider(LocalDate selectedDateOfAverage, WeatherProvider provider) {
        return forecastRepository.findByDateAndProvider(selectedDateOfAverage, provider);
    }

    public List<ForeCast> AverageAllDataBase(LocalDate selectedDateOfAverage) {
        return forecastRepository.findByDate(selectedDateOfAverage);
    }


    public Optional<ForeCast> get(UUID id) throws IOException {  //används vid get via api fetch 8080
        return getForeCasts().stream().filter(forecast->forecast.getId().equals(id)).findFirst();
    }

}
