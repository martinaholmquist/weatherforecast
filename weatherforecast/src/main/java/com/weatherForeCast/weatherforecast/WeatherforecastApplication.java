package com.weatherForeCast.weatherforecast;
import com.weatherForeCast.weatherforecast.services.userAdministrationServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;



@SpringBootApplication
public class WeatherforecastApplication implements CommandLineRunner {


    @Autowired  //containern gör en abstraction som gör att det new:ar per automatik
    userAdministrationServices userAdministrationServices;



    public static void main(String[] args) {
        SpringApplication.run(WeatherforecastApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        userAdministrationServices.menu();


    }
}

