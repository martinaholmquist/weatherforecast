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

/*  gömmer den för att kolla om allt med update funkar
    public void update(ForeCast foreCastFromUser) {
        UUID idToUpdate = foreCastFromUser.getId();

        // letar efter id:t
        Optional<ForeCast> optionalForecast = forecastRepository.findById(idToUpdate);

        if (optionalForecast.isPresent()) {
            // Updatetea värdena med de nya värdena
            ForeCast existingForecast = optionalForecast.get();
            existingForecast.setTemperature(foreCastFromUser.getTemperature());
            existingForecast.setHour(foreCastFromUser.getHour());
            existingForecast.setDate(foreCastFromUser.getDate());
            existingForecast.setProvider(foreCastFromUser.getProvider());
            existingForecast.setLatitude(foreCastFromUser.getLatitude());
            existingForecast.setLongitude(foreCastFromUser.getLongitude());
            existingForecast.setPrecipitation(foreCastFromUser.isPrecipitation());
            existingForecast.setCreated(foreCastFromUser.getCreated());

            // Save the updated forecast to the database
            forecastRepository.save(existingForecast);

        } else {
            // om inget id finns
            System.out.println("Forecast with ID " + idToUpdate + " not found.");
        }
    }
*/

    public void updateWithDTO(ForeCast foreCastFromUser) throws IOException {

        UUID idToUpdate = foreCastFromUser.getId();

        // letar efter id:t
        Optional<ForeCast> optionalForecast = forecastRepository.findById(idToUpdate);

        if (optionalForecast.isPresent()) {
            // Updatetea värdena med de nya värdena
            ForeCast existingForecast = optionalForecast.get();
            existingForecast.setTemperature(foreCastFromUser.getTemperature());
            existingForecast.setHour(foreCastFromUser.getHour());
            existingForecast.setDate(foreCastFromUser.getDate());
            existingForecast.setProvider(foreCastFromUser.getProvider());
            existingForecast.setPrecipitation(foreCastFromUser.isPrecipitation());

            // spara till databasen
            forecastRepository.save(existingForecast);

        } else {
            // om inget id finns
            System.out.println("Forecast with ID " + idToUpdate + " not found.");
        }

    }


    //ANVÄNDS EJ
    public List<ForeCast> getForeCastsTest() throws IOException {
        return forecastRepository.findAll();
    }


    public List<ForeCast> getForeCasts() throws IOException {
        List<ForeCast> orderedForecasts = forecastRepository.findAllOrdered();
       // orderedForecasts.forEach(valueAll -> System.out.println(valueAll));
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


    public void deleteWeather(UUID id) throws IOException {  //denna kan jag ta bort egentligen då vi ALDRIG ska deleta utan ska lägga vilande eller non active etc.....
        forecastRepository.deleteById(id);
    }

    public  ForeCast add(ForeCast foreCast) throws IOException {
        forecastRepository.save(foreCast);
        return foreCast;
    }


    //TESTAR ATT HA DEM HÄR ISTÄLLET

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

                // Mark this hour as processed
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

                // Mark this hour as processed
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
    //TILL HIT


    public List<ForeCast> getAverageByProvider(LocalDate selectedDateOfAverage, WeatherProvider provider) {
        return forecastRepository.findByDateAndProvider(selectedDateOfAverage, provider);
    }

    public List<ForeCast> AverageAllDataBase(LocalDate selectedDateOfAverage) {
        return forecastRepository.findByDate(selectedDateOfAverage);
    }


    public Optional<ForeCast> get(UUID id) throws IOException {  //används vid get via api fetch 8080
        return getForeCasts().stream().filter(forecast->forecast.getId().equals(id)).findFirst();
    } //Hämtar med inmatat id används vid get via api fetch 8080







    /*   ALLT DETTA ÄR FIL
    public ForeCastServices() throws IOException {
        try {
            foreCasts = readFromFile()
            ;    }catch (IOException e){
            throw new RuntimeException();
        }

    }  //read from file

    private List<ForeCast> readFromFile() throws IOException {
        if(!Files.exists(Path.of("predictions.json"))) return new ArrayList<ForeCast>();
        ObjectMapper objectMapper = getObjectMapper();
        var jsonStr = Files.readString(Path.of("predictions.json"));
        return  new ArrayList(Arrays.asList(objectMapper.readValue(jsonStr, ForeCast[].class ) ));
    }  //read from file

    //add to file
    private static void writeAllToFile(List<ForeCast> weatherPredictions) throws IOException {
        ObjectMapper objectMapper = getObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);


        StringWriter stringWriter = new StringWriter();
        objectMapper.writeValue(stringWriter, weatherPredictions);

        Files.writeString(Path.of("predictions.json"), stringWriter.toString());

    }

    // gör så att json kan läsa  LocalDate , LocalTime  Kallas på i writeAllToFile()
    private static ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        return mapper;
    }*/

/*
    public List<ForeCast>getForeCasts() throws IOException {
        //methodServices.SMHItoMySQL();
        List<ForeCast> orderedForecasts = forecastRepository.findAllOrdered();
        orderedForecasts.forEach(valueAll -> System.out.println(valueAll));
        return foreCasts;
    }*/



    /*
    public List<ForeCast> getAverageByProvider(LocalDate selectedDateOfAverage, WeatherProvider provider) {
        forecastRepository.findByDateAndProvider(selectedDateOfAverage, provider);
        return null;
    }*/



/*
    //denna är för filsparade forecasts, används för att testa!!!!!
    public static List<ForeCast>getForeCastsFromFile() throws IOException {  //hämtar ovan lista som är private
        foreCasts.stream().sorted(Comparator.comparing(ForeCast::getDate)).forEach(weather -> System.out.println(weather.getDate()));
        return foreCasts;
    }*/







    //till hit......

/*
    public ForeCast getByIndex(int i){
        return foreCasts.get(i);
    }*/





    /*
    public static ForeCast addProduct(ForeCast foreCast) throws IOException {  //vet ej vad denna används till.....
        foreCasts.add(foreCast);
        writeAllToFile(foreCasts);
        return foreCast;
    }
*/

    /*uppdatera till fil
    public void updateToFile (ForeCast foreCastFromUser) throws IOException { //denna sparar så vi senare kan spara i DB
        //funkar inte, måste ta den nya forecasten och överskriva den gamla via en array och för varje variabel.

        var forecastinList = get(foreCastFromUser.getId()).get();
        forecastinList.setTemperature(foreCastFromUser.getTemperature());
        forecastinList.setHour(foreCastFromUser.getHour());
        forecastinList.setDate(foreCastFromUser.getDate());
        writeAllToFile(foreCasts);
    }*/






}
