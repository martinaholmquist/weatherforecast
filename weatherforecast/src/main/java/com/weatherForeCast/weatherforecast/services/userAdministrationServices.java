package com.weatherForeCast.weatherforecast.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weatherForeCast.weatherforecast.METmodels.RootMET;
import com.weatherForeCast.weatherforecast.METmodels.Timeseries;

import com.weatherForeCast.weatherforecast.SMHImodels.RootSMHI;
import com.weatherForeCast.weatherforecast.SMHImodels.Parameter;
import com.weatherForeCast.weatherforecast.SMHImodels.TimeSeries;
import com.weatherForeCast.weatherforecast.MainModule.ForeCast;

import com.weatherForeCast.weatherforecast.dto.AverageDTO;
import com.weatherForeCast.weatherforecast.provider.WeatherProvider;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;



@Service
public class userAdministrationServices {

    @Autowired  //containern gör en abstraction som gör att det new:ar per automatik
    ForeCastServices foreCastServices;


    public void menu() throws IOException {
        Scanner scan = new Scanner(System.in);
        boolean stayInApp = true;
        while (stayInApp) {
            showMenu();
            System.out.print("Please make a choice:");
            System.out.println();
            int choice = scan.nextInt();
            if (choice == 1) {
                SMHItoMySQL();
            } else if (choice == 2) {
                METtoMySQL();
            } else if (choice == 3) {
                listAllForeCastFromMySQL();
            } else if (choice == 4) {
                createForeCastToMySQLUser();   //funkar
            } else if (choice == 5) {
                updateForeCast();
            } else if (choice == 6) {
                deleteForeCastUserInput();
            } else if (choice == 7) {
                getAverageUserInput();
            } else if (choice == 8) {
                //METtoMySQL();

            } else if (choice == 9) {
                System.out.println("Thank you for using my App ");
                stayInApp = false;
                break;
            } else System.out.println("Invalid input, 1-4 or 9 please");
        }
    }

    private void showMenu() {
        System.out.println("\n1. Add SMHI to SQL");
        System.out.println("2. Add MET to SQL");
        System.out.println("3. List all forecasts in SQL");
        System.out.println("4. Create forecast and save to SQL");
        System.out.println("5. Update forecast");
        System.out.println("6. Delete a forecast");
        System.out.println("7. Get average by date");
        System.out.println("8. ");

        System.out.println("9. Exit");
    }

    private void displayForecastsWithNum(List<ForeCast> forecasts) {
        int num = 1;
        for (var prediction : forecasts) {
            System.out.printf("Itemno:%d date:%s kl:%s temp:%.2f°C %n",
                    num,
                    prediction.getDate(),
                    prediction.getHour(),
                    prediction.getTemperature()
            );
            num++;
        }
    }


    public void SMHItoMySQL() throws IOException {

        try {
        var objectMapper = new ObjectMapper();

        RootSMHI smhivariabel = objectMapper.readValue(new URL("https://opendata-download-metfcst.smhi.se/api/category/pmp3g/version/2/geotype/point/lon/18.021515/lat/59.309965/data.json"), RootSMHI.class);
        List<TimeSeries> smhiLista = smhivariabel.getTimeSeries();

        var objectWriter = objectMapper.writerWithDefaultPrettyPrinter();

        String fetchedProduct = objectWriter.writeValueAsString(smhivariabel.getGeometry().getCoordinates());

        // parsa för att kunna ta ut delar av det, som long och lat
        JsonNode jsonNode = objectMapper.readTree(fetchedProduct);  //readtree tar ut information som man innan inte riktigt vet hur JSON datan ser ut
        float longitude = jsonNode.get(0).get(0).floatValue();
        float latitude = jsonNode.get(0).get(1).floatValue();


        LocalDate date;
        LocalTime hour;
        float temperature;
        boolean precipitation;

        for (TimeSeries smhi : smhiLista) {
            Date validTimeDate = smhi.getValidTime();
            Instant instant = validTimeDate.toInstant();
            ZoneId zoneId = ZoneId.of("UTC"); // justera till rätt tidszon
            LocalDateTime dateTime = instant.atZone(zoneId).toLocalDateTime();


            if (dateTime.isAfter(LocalDateTime.now()) && dateTime.isBefore(LocalDateTime.now().plusHours(24))) {
                //om forecasten är inom ranget fortsätt
            } else {
                //om forecasten är innan eller efter skippa
                continue; // gå vidare till nästa
            }

            date = dateTime.toLocalDate(); // hämta datum
            hour = dateTime.toLocalTime(); // hämta tid

            temperature = -1; // Default value
            precipitation = false; // Default value

            for (Parameter parameter : smhi.getParameters()) {
                if (parameter.getName().equals("t")) {
                    temperature = parameter.getValues().get(0).floatValue();
                }
                if (parameter.getName().equals("pcat")) {
                    double precipitationValue = parameter.getValues().get(0);
                    precipitation = precipitationValue > 0.0;
                }
            }

            LocalDateTime created = LocalDateTime.now();
            //int provider = 1;


            ForeCast foreCastFromSMHI = new ForeCast(date, hour, temperature, created, longitude, latitude, precipitation, WeatherProvider.SMHI);
            //forecastRepository.save(foreCastFromSMHI);
            foreCastServices.add(foreCastFromSMHI);

        }
            System.out.println("SMHI forecasts is added to database");
    }
     catch (IOException e) {
        e.printStackTrace();
        System.err.println("Error while fetching data from SMHI.");
    } catch (Exception e) {
        e.printStackTrace();
        System.err.println("An unexpected error.");
    }
}

    public void METtoMySQL() {
        try {
            var objectMapper = new ObjectMapper();


            URL url = new URL("https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=59.309965&lon=18.021515");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // useragent
            connection.setRequestProperty("User-Agent", "weatherforecast/1.0 martina.holmqwist@gmail.com");


            RootMET metVariabel = objectMapper.readValue(connection.getInputStream(), RootMET.class);

            double longitude = metVariabel.geometry.coordinates.get(0);
            double latitude = metVariabel.geometry.coordinates.get(1);

            // Hämta metLista från metVariabel
            List<Timeseries> metLista = metVariabel.properties.timeseries;

            // Iterate through MET data and insert into MySQL
            for (Timeseries met : metLista) {
                Date validTimeDate = met.time;
                Instant instant = validTimeDate.toInstant();
                ZoneId zoneId = ZoneId.of("UTC"); // Justera till rätt tidszon
                LocalDateTime dateTime = instant.atZone(zoneId).toLocalDateTime();

                // Lägg till villkoret för att kontrollera om forecasten är inom det önskade tidsintervallet
                if (dateTime.isAfter(LocalDateTime.now()) && dateTime.isBefore(LocalDateTime.now().plusHours(24))) {
                    float temperature = (float) met.data.instant.details.air_temperature;

                    boolean precipitation = met.data.instant.details.precipitation_amount > 0.0;

                    LocalDateTime created = LocalDateTime.now();
                    int provider = 2;

                    ForeCast foreCastFromMET = new ForeCast(dateTime.toLocalDate(), dateTime.toLocalTime(), temperature, created, (float) longitude, (float) latitude, precipitation, WeatherProvider.MET);
                    foreCastServices.add(foreCastFromMET);


                }

            }
            System.out.println("MET forecasts is added to database");
        }
        catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error while fetching data from MET.");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("An unexpected error.");
        }
    }


    private void listAllForeCastFromMySQL() throws IOException {
        try{
            //foreCastServices.getForeCasts();
            foreCastServices.getForeCasts().forEach(valueAll -> System.out.println(valueAll));
        }catch (IOException e) {
        e.printStackTrace();
        System.err.println("Error while listing forecasts.");
    } catch (Exception e) {
        e.printStackTrace();
        System.err.println("An unexpected error.");
    }
    }

    private void createForeCastToMySQLUser() throws IOException {


        try {
            Scanner scan = new Scanner(System.in);
            System.out.println("Create an weather forecast");
            System.out.println();
            System.out.println("Please enter date (yyyy-MM-dd)");
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String dateString = scan.nextLine();
            LocalDate date = LocalDate.parse(dateString, dateFormatter);
            System.out.println("Please enter time (HH:mm)");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            String timeString = scan.nextLine();
            LocalTime hour = LocalTime.parse(timeString, timeFormatter);
            System.out.println("Please enter temperature");
            float temperature = scan.nextFloat();
            System.out.println("Please enter longitude");
            float longitude = scan.nextFloat();
            System.out.println("Please enter latitude");
            float latitude = scan.nextFloat();
            System.out.println("Is it raining or snowing? Enter 1 for Yes, 0 for No:");
            int precipitationInput = scan.nextInt();
            boolean precipitation = precipitationInput >= 1; // Convertera till boolean
            LocalDateTime created = LocalDateTime.now();


            //med enum
            ForeCast foreCastFromUser = new ForeCast(date, hour, temperature, created, longitude, latitude, precipitation, WeatherProvider.USER);
            foreCastServices.add(foreCastFromUser);

            System.out.println("Forecast created:");
            System.out.println(foreCastFromUser);

        } catch (Exception e) {
            System.out.println("Error occurred:");
            e.printStackTrace();
        }

    }

    private void updateForeCast() throws IOException {
        Scanner scan = new Scanner(System.in);

        List<ForeCast> orderedForecasts = foreCastServices.getForeCasts();
        displayForecastsWithNum(orderedForecasts);

        System.out.println("Please enter the item number you'd like to update:");
        int updateID = scan.nextInt();

        System.out.println("Please choose what you'd like to update:");
        System.out.println("1 = date");
        System.out.println("2 = hour");
        System.out.println("3 = temperature");

        int choice = scan.nextInt();

        switch (choice) {
            case 1:
                updateDate(orderedForecasts, updateID, scan);
                break;
            case 2:
                updateTime(orderedForecasts, updateID, scan);
                break;
            case 3:
                updateTemp(orderedForecasts, updateID, scan);
                break;
            default:
                System.out.println("Invalid choice");
        }
    }

    @Transactional  //for updates and deletes
    private void updateTime(List<ForeCast> forecasts, int updateID, Scanner scan) throws IOException {
        scan.nextLine();
        System.out.println("Please enter new value (HH:mm):");
        String timeString = scan.nextLine();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime hour = LocalTime.parse(timeString, timeFormatter);

        if (updateID >= 1 && updateID <= forecasts.size()) {
            ForeCast predictionToUpdate = forecasts.get(updateID - 1);
            predictionToUpdate.setHour(hour);
            System.out.println("Updated time for item " + updateID + " to " + hour);

            try {
                foreCastServices.updateForecastByTime(predictionToUpdate.getId(), hour);
            } catch (Exception e) {
                System.out.println("error in update");
                e.printStackTrace();
            }
        } else {
            System.out.println("Invalid item number.");
        }
    }

    @Transactional  //for updates and deletes
    private void updateTemp(List<ForeCast> forecasts, int updateID, Scanner scan) throws IOException {
        scan.nextLine();
        System.out.println("Please enter new temperature:");
        float newTemperature = scan.nextFloat();

        if (updateID >= 1 && updateID <= forecasts.size()) {
            ForeCast predictionToUpdate = forecasts.get(updateID - 1);
            predictionToUpdate.setTemperature(newTemperature);
            System.out.println("Updated temperature for item " + updateID + " to " + newTemperature);

            try {
                foreCastServices.updateForecastByTemperature(predictionToUpdate.getId(), newTemperature);
            } catch (Exception e) {
                System.out.println("Error in update");
                e.printStackTrace();
            }
        } else {
            System.out.println("Invalid item number.");
        }

    }

    @Transactional  //for updates and deletes
    private void updateDate(List<ForeCast> forecasts, int updateID, Scanner scan) throws IOException {
        scan.nextLine();
        System.out.println("Please enter new value (yyyy-MM-dd):");
        String dateString = scan.nextLine();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate newDate = LocalDate.parse(dateString, dateFormatter);

        if (updateID >= 1 && updateID <= forecasts.size()) {
            ForeCast predictionToUpdate = forecasts.get(updateID - 1);
            predictionToUpdate.setDate(newDate);
            System.out.println("Updated date for item " + updateID + " to " + newDate);

            try {
                foreCastServices.updateForecastByDate(predictionToUpdate.getId(), newDate);
            } catch (Exception e) {
                System.out.println("error in update");
                e.printStackTrace();
            }
        } else {
            System.out.println("Invalid item number.");
        }
    }

    private void deleteForeCastUserInput() throws IOException {
        try{
        Scanner scan = new Scanner(System.in);
        List<ForeCast> orderedForecasts = foreCastServices.getForeCasts();
        displayForecastsWithNum(orderedForecasts);

        System.out.println("Please enter the item number you'd like to delete:");
        int deleteID = scan.nextInt();

        deleteForecast(orderedForecasts, deleteID);
        }catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error while deleting.");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("An unexpected error.");
        }
    }

    public void deleteForecast(List<ForeCast> forecasts, int deleteID) {
        if (deleteID >= 1 && deleteID <= forecasts.size()) {
            ForeCast predictionToDelete = forecasts.get(deleteID - 1);

            try {
                foreCastServices.deleteWeather(predictionToDelete.getId());
                System.out.println("Forecast with ID " + deleteID + " has been deleted.");
            } catch (Exception e) {
                System.out.println("error in delete");
                e.printStackTrace();
            }
        } else {
            System.out.println("Invalid item number.");
        }

    }

    public void getAverageUserInput() throws IOException {
        Scanner scan = new Scanner(System.in);
        try {
        System.out.println("Please enter the date you'd like to have an average from:");
        String dateString = scan.nextLine();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate DateOfAverage = LocalDate.parse(dateString, dateFormatter);

        //List<AverageDTO> averageDTOs = getAverageByDateDTO(DateOfAverage);  //ORIGINAL INNAN FLYTT
            List<AverageDTO> averageDTOs = foreCastServices.getAverageByDateDTO(DateOfAverage);

        if (averageDTOs.isEmpty()) {
            System.out.println("No average data available for the selected date.");
        } else {
            System.out.println("Average temperature for the selected date:");

            for (int i = 0; i < averageDTOs.size(); i++) {
                AverageDTO averageDTO = averageDTOs.get(i);
                System.out.println("Time: " + averageDTO.getHour() + ", Temperature: " + averageDTO.getAverageTemperature()+"°C");
            }
        }
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Please enter the date in yyyy-MM-dd format.");
        }
    }



    //TESTAR ATT FLYTTA DESSA TILL fOREcASTsERVICES

    /*

    public List<AverageDTO> getAverageByDateDTO(LocalDate selectedDateOfAverage) {
        List<ForeCast> averageForecast = foreCastServices.AverageAllDataBase(selectedDateOfAverage);

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
        List<ForeCast> averageForecast = foreCastServices.getAverageByProvider(selectedDateOfAverage, provider);

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

        List<ForeCast> allForeCastsInDB = foreCastServices.getForeCasts();


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

*/



    //ta bort denna, den funkar men skriver ut string
    public String AverageTempAndPrecipitationByHourAndTimeAllDataBase(LocalDate selectedDateOfAverage) {
        // Hämta en lista med väderprognoser för den angivna dagen
        List<ForeCast> averageForecast = foreCastServices.AverageAllDataBase(selectedDateOfAverage); // Använder en tjänst för att hämta prognoser

        // Sortera listan efter timmen för varje prognos
        averageForecast.sort(Comparator.comparing(ForeCast::getHour));

        // Om listan är tom, returnera ett meddelande om att det inte finns några prognoser
        if (averageForecast.isEmpty()) {
            return "No forecasts available for " + selectedDateOfAverage;
        }

        // Skapa en lista för att lagra resultaten för varje timme
        List<String> ListOfAllResults = new ArrayList<>();

        // Loopa igenom varje prognos i den sorterade listan
        for (ForeCast forecast : averageForecast) {
            LocalTime hour = forecast.getHour();

            // Kontrollera om timmen redan finns i ListOfAllResults för att det inte ska skrivas ut flera gånger
            boolean hourExists = false;
            for (String result : ListOfAllResults) {
                if (result.contains("Hour: " + hour)) {
                    hourExists = true;
                    break;
                }
            }

            // Om timmen inte finns, beräkna och lägg till informationen
            if (!hourExists) {
                double totalTemperature = 0.0;
                int precipitationTrueCount = 0;
                int precipitationFalseCount = 0;

                /*
                a. Den startar en ny loop för att räkna alla förekomster av timmen i averageForecast.
                b. För varje forecastHour i averageForecast jämför den forecastHour.getHour() med hour (den aktuella timmen som undersöks).
                c. Om forecastHour.getHour().equals(hour) är sant, lägger den till forecastHour.getTemperature() till totalTemperature och räknar antalet nederbördshändelser (precipitation) baserat på värdet av forecastHour.isPrecipitation().
                d. När den har gått igenom alla förekomster av timmen, beräknar den det genomsnittliga värdet av temperaturerna och nederbördshändelserna för den timmen.
                e. Sedan skapas en sträng med information om timmen, dess genomsnittstemperatur och nederbördstatus, som läggs till i ListOfAllResults.
                */
                for (ForeCast forecastHour : averageForecast) {
                    if (forecastHour.getHour().equals(hour)) {
                        totalTemperature += forecastHour.getTemperature();
                        if (forecastHour.isPrecipitation()) {
                            precipitationTrueCount++;
                        } else {
                            precipitationFalseCount++;
                        }
                    }
                }

                double averageTemperature = totalTemperature / (precipitationTrueCount + precipitationFalseCount);

                String precipitationStatus;
                if (precipitationTrueCount > precipitationFalseCount) {
                    precipitationStatus = "Precipitation is happening";
                } else if (precipitationTrueCount < precipitationFalseCount) {
                    precipitationStatus = "No precipitation";
                } else {
                    precipitationStatus = "Mixed precipitation";
                }

                String averageInfo = "Hour: " + hour + " - Average temperature: " +
                        String.format("%.2f", averageTemperature) + "°C. " +
                        precipitationStatus;

                //här ska jag adda på till en ny lista DTO som innehåller datan.
                ListOfAllResults.add(averageInfo);
            }
        }

        // Returnera en sammanfattning av resultaten för varje timme
        return String.join("\n", ListOfAllResults);
    }
    public String getAverageFromWhatEverIsTypedInOriginal(LocalDate selectedDateOfAverage, WeatherProvider provider) {
        List<ForeCast> forecastsForDay = foreCastServices.getAverageByProvider(selectedDateOfAverage, provider);

        double totalTemperature = 0.0;
        int precipitationTrueCount = 0;
        int precipitationFalseCount = 0;

        for (ForeCast forecast : forecastsForDay) {
            totalTemperature += forecast.getTemperature();
            if (forecast.isPrecipitation()) {
                precipitationTrueCount++;
            } else {
                precipitationFalseCount++;
            }
        }

        int numberOfForecasts = forecastsForDay.size();

        if (numberOfForecasts > 0) {
            double averageTemperature = totalTemperature / numberOfForecasts;

            String precipitationStatus;
            if (precipitationTrueCount > precipitationFalseCount) {
                precipitationStatus = "Precipitation is happening";
            } else if (precipitationTrueCount < precipitationFalseCount) {
                precipitationStatus = "No precipitation";
            } else {
                precipitationStatus = "Mixed precipitation";
            }

            String averageInfo = "Info from getAverageFromWhatEverIsTypedIn:  Average temperature for " + selectedDateOfAverage + ": " +
                    String.format("%.2f", averageTemperature) + "°C. \n" +
                    precipitationStatus + " on " + selectedDateOfAverage;
            System.out.println(averageInfo);
            return averageInfo;
        } else {
            return "No forecasts available for " + selectedDateOfAverage;
        }
    }

    public void METtoConsole() {
        try {
            var objectMapper = new ObjectMapper();

            // Replace with the actual MET API URL
            RootMET metVariabel = objectMapper.readValue(new URL("https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=59.309965&lon=18.021515"), RootMET.class);
            List<com.weatherForeCast.weatherforecast.METmodels.Timeseries> metLista = metVariabel.properties.timeseries;

            // Example: Print data from the first entry
            if (!metLista.isEmpty()) {
                System.out.println("First MET entry:");
                System.out.println("Temperature: " + metLista.get(0).data.instant.details.air_temperature);
                System.out.println("Precipitation Amount: " + metLista.get(0).data.instant.details.precipitation_amount);
            } else {
                System.out.println("No MET data available.");
            }
        } catch (IOException e) {
            // Handle IO exception (e.g., network issues)
            e.printStackTrace();
        } catch (Exception ex) {
            // Handle other exceptions
            ex.printStackTrace();
        }
    }
    public void METtoMySQL1() {
        try {
            var objectMapper = new ObjectMapper();

            // Skapa en URL-anslutning
            URL url = new URL("https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=59.309965&lon=18.021515");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // useragent
            connection.setRequestProperty("User-Agent", "weatherforecast/1.0 martina.holmqwist@gmail.com");

            // Läs data från anslutningen och bearbeta den med ObjectMapper
            RootMET metVariabel = objectMapper.readValue(connection.getInputStream(), RootMET.class);

            // Extract longitude and latitude from MET data
            double longitude = metVariabel.geometry.coordinates.get(0);
            double latitude = metVariabel.geometry.coordinates.get(1);

            // Hämta metLista från metVariabel
            List<com.weatherForeCast.weatherforecast.METmodels.Timeseries> metLista = metVariabel.properties.timeseries;

            // Iterate through MET data and insert into MySQL
            for (com.weatherForeCast.weatherforecast.METmodels.Timeseries met : metLista) {
                Date validTimeDate = met.time;
                Instant instant = validTimeDate.toInstant();
                ZoneId zoneId = ZoneId.of("UTC"); // Justera till rätt tidszon
                LocalDateTime dateTime = instant.atZone(zoneId).toLocalDateTime();

                float temperature = (float) met.data.instant.details.air_temperature;

                boolean precipitation = met.data.instant.details.precipitation_amount > 0.0;

                LocalDateTime created = LocalDateTime.now();
                int provider = 2;

                ForeCast foreCastFromMET = new ForeCast(dateTime.toLocalDate(), dateTime.toLocalTime(), temperature, created, (float) longitude, (float) latitude, precipitation, WeatherProvider.MET);
                //foreCastServices.add(foreCastFromMET);
                System.out.println(foreCastFromMET);
            }

        } catch (IOException e) {
            // Hantera IO-fel (t.ex. nätverksproblem)
            e.printStackTrace();
        } catch (Exception ex) {
            // Hantera andra undantag
            ex.printStackTrace();
        }
    }



}






