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

    @Autowired  //containern gör en abstraction som gör att det new:ar per automatiskt
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
                createForeCastToMySQLUser();
            } else if (choice == 5) {
                updateForeCast();
            } else if (choice == 6) {
                deleteForeCastUserInput();
            } else if (choice == 7) {
                getAverageUserInput();

            } else if (choice == 9) {
                System.out.println("Thank you for using my App ");
                stayInApp = false;
                break;
            } else System.out.println("Invalid input, 1-7 or 9 please");
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

                temperature = -1;
                precipitation = false;

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


                ForeCast foreCastFromSMHI = new ForeCast(date, hour, temperature, created, longitude, latitude, precipitation, WeatherProvider.SMHI);
                foreCastServices.add(foreCastFromSMHI);

            }
            System.out.println("SMHI forecasts is added to database");
        } catch (IOException e) {
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

                    ForeCast foreCastFromMET = new ForeCast(dateTime.toLocalDate(), dateTime.toLocalTime(), temperature, created, (float) longitude, (float) latitude, precipitation, WeatherProvider.MET);
                    foreCastServices.add(foreCastFromMET);


                }

            }
            System.out.println("MET forecasts is added to database");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error while fetching data from MET.");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("An unexpected error.");
        }
    }


    private void listAllForeCastFromMySQL() throws IOException {
        try {
            foreCastServices.getForeCasts().forEach(valueAll -> System.out.println(valueAll));
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error while listing forecasts.");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("An unexpected error.");
        }
    }

    private void createForeCastToMySQLUser() throws IOException {
        while (true) {
            try {
                System.out.println("Create a weather forecast");
                System.out.println();
                LocalDate date = getDateFromUser();
                LocalTime hour = getTimeFromUser();
                float temperature = getTemperatureFromUser();
                float longitude = getLongitudeFromUser();
                float latitude = getLatitudeFromUser();
                int precipitationInput = getPrecipitationInputFromUser();
                boolean precipitation = (precipitationInput == 1); //true eftersom (1 == 1) är sant, false eftersom (0 == 1) är falskt.
                LocalDateTime created = LocalDateTime.now();

                ForeCast foreCastFromUser = new ForeCast(date, hour, temperature, created, longitude, latitude, precipitation, WeatherProvider.USER);
                foreCastServices.add(foreCastFromUser);

                System.out.println("Forecast created:");
                System.out.println(foreCastFromUser);

                break; // Avsluta loopen när alla värden är giltiga
            } catch (Exception e) {
                System.out.println("An error occurred:");
                e.printStackTrace();
            }
        }
    }
    private LocalDate getDateFromUser() {
        Scanner scan = new Scanner(System.in);
        LocalDate date = null;

        while (date == null) {
            try {
                System.out.println("Please enter date (yyyy-MM-dd)");
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                String dateString = scan.nextLine();
                date = LocalDate.parse(dateString, dateFormatter);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please enter the date in yyyy-MM-dd format.");
            }
        }

        return date;
    }
    private LocalTime getTimeFromUser() {
        Scanner scan = new Scanner(System.in);
        LocalTime hour = null;

        while (hour == null) {
            try {
                System.out.println("Please enter time (HH:mm)");
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                String timeString = scan.nextLine();
                hour = LocalTime.parse(timeString, timeFormatter);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid time format. Please enter the date in HH:mm format.");
            }
        }

        return hour;
    }
    private float getTemperatureFromUser() {
        Scanner scan = new Scanner(System.in);
        float temperature = 0;
        boolean validInput = false;

        while (!validInput) {
            try {
                System.out.println("Please enter temperature");
                temperature = Float.parseFloat(scan.nextLine());

                if (temperature < -100 || temperature > 100) {
                    System.out.println("Invalid temperature. Please enter a value between -100 and 100.");
                } else {
                    validInput = true;
                }

            } catch (NumberFormatException e) {
                System.out.println("Invalid input format. Please enter a value between -100 and 100.");
            }
        }

        return temperature;
    }
    private float getLongitudeFromUser() {
        Scanner scan = new Scanner(System.in);
        float longitude = 0;
        boolean validInput = false;

        while (!validInput) {
            try {
                System.out.println("Please enter longitude");
                 longitude = Float.parseFloat(scan.nextLine());
                    validInput = true;

            } catch (NumberFormatException e) {
                System.out.println("Invalid input format. Please enter a number.");
            }
        }

        return longitude;
    }
    private float getLatitudeFromUser() {
        Scanner scan = new Scanner(System.in);
        float latitude = 0;
        boolean validInput = false;

        while (!validInput) {
            try {
                System.out.println("Please enter longitude");
                latitude = Float.parseFloat(scan.nextLine());
                    validInput = true;


            } catch (NumberFormatException e) {
                System.out.println("Invalid input format. Please enter a number.");
            }
        }

        return latitude;
    }
    private int getPrecipitationInputFromUser() {
        Scanner scan = new Scanner(System.in);
        int precipitationInput = -1;

        while (precipitationInput != 0 && precipitationInput != 1) {
            try {
                System.out.println("Is it raining or snowing? Enter 1 for Yes, 0 for No:");
                String input = scan.nextLine();

                if (input.equals("0")) {
                    precipitationInput = 0;
                } else if (input.equals("1")) {
                    precipitationInput = 1;
                } else {
                    System.out.println("Invalid precipitation input. Please enter either 0 for No or 1 for Yes.");
                }
            } catch (Exception e) {
                System.out.println("An error occurred:");
                e.printStackTrace();
            }
        }

        return precipitationInput;
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

    private void deleteForeCastUserInput() {
        try {
            Scanner scan = new Scanner(System.in);
            List<ForeCast> orderedForecasts = foreCastServices.getForeCasts();
            displayForecastsWithNum(orderedForecasts);

            System.out.println("Please enter the item number you'd like to delete:");
            int deleteID = scan.nextInt();

            deleteForecast(orderedForecasts, deleteID);
        } catch (IOException e) {
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

            List<AverageDTO> averageDTOs = foreCastServices.getAverageByDateDTO(DateOfAverage);

            if (averageDTOs.isEmpty()) {
                System.out.println("No average data available for the selected date.");
            } else {
                System.out.println("Average temperature for the selected date:");

                for (int i = 0; i < averageDTOs.size(); i++) {
                    AverageDTO averageDTO = averageDTOs.get(i);
                    System.out.println("Time: " + averageDTO.getHour() + ", Temperature: " + averageDTO.getAverageTemperature() + "°C");
                }
            }
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Please enter the date in yyyy-MM-dd format.");
        }
    }




    private void createForeCastToMySQLUserORIGINAL() throws IOException {

        Scanner scan = new Scanner(System.in);

        while (true) {
            try {
                System.out.println("Create a weather forecast");
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
                float temperature = Float.parseFloat(scan.nextLine());

                if (temperature < -100 || temperature > 100) {
                    throw new InputMismatchException("Invalid temperature. Please enter a value between -100 and 100.");
                }

                System.out.println("Please enter longitude");
                float longitude = Float.parseFloat(scan.nextLine());

                if (longitude < -180 || longitude > 180) {
                    throw new InputMismatchException("Invalid longitude. Please enter a value between -180 and 180.");
                }

                System.out.println("Please enter latitude");
                float latitude = Float.parseFloat(scan.nextLine());

                if (latitude < -90 || latitude > 90) {
                    throw new InputMismatchException("Invalid latitude. Please enter a value between -90 and 90.");
                }

                int precipitationInput;

                while (true) {
                    System.out.println("Is it raining or snowing? Enter 1 for Yes, 0 for No:");
                    String input = scan.nextLine();

                    if (input.equals("0")) {
                        precipitationInput = 0;
                        break;
                    } else if (input.equals("1")) {
                        precipitationInput = 1;
                        break;
                    } else {
                        System.out.println("Invalid precipitation input. Please enter either 0 for No or 1 for Yes.");
                    }
                }

                boolean precipitation = precipitationInput == 1;
                LocalDateTime created = LocalDateTime.now();

                ForeCast foreCastFromUser = new ForeCast(date, hour, temperature, created, longitude, latitude, precipitation, WeatherProvider.USER);
                foreCastServices.add(foreCastFromUser);

                System.out.println("Forecast created:");
                System.out.println(foreCastFromUser);

                break; // Avsluta loopen när alla värden är giltiga
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date or time format. Please enter the date in yyyy-MM-dd and time in HH:mm format.");
            } catch (InputMismatchException e) {
                System.out.println("Invalid input format. Please enter valid numeric values.");
            } catch (Exception e) {
                System.out.println("An error occurred:");
                e.printStackTrace();
            }
        }
    }
}






