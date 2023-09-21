package com.weatherForeCast.weatherforecast.controller;

import com.weatherForeCast.weatherforecast.MainModule.ForeCast;
import com.weatherForeCast.weatherforecast.dto.AverageDTO;
import com.weatherForeCast.weatherforecast.dto.ViewForeCastDTO;
import com.weatherForeCast.weatherforecast.provider.WeatherProvider;
import com.weatherForeCast.weatherforecast.services.ForeCastServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;


@RestController
public class ForeCastControllerAPI {

    @Autowired
    ForeCastServices foreCastServices;



    // DTO
    @GetMapping("/api/forecasts/average/{selectedDate}")
    public ResponseEntity<List<AverageDTO>> getAverageDTO(@PathVariable("selectedDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate selectedDate) {
        List<AverageDTO> resultAverageDTOList = foreCastServices.getAverageByDateDTO(selectedDate);

        if (!resultAverageDTOList.isEmpty()) {
            return ResponseEntity.ok(resultAverageDTOList);
        }

        return ResponseEntity.notFound().build();
    }


    @GetMapping("/api/forecasts/average/{selectedProvider}/{selectedDate}")
    public ResponseEntity<List<AverageDTO>> getAverageFromWhatEverIsTypedInDTO(
            @PathVariable("selectedProvider") String providerString,
            @PathVariable("selectedDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate selectedDate) {

        WeatherProvider provider = WeatherProvider.valueOf(providerString.toUpperCase());
        List<AverageDTO> resultAverageDTOList = foreCastServices.getAverageFromWhatEverIsTypedInDTO(selectedDate, provider);

        if (!resultAverageDTOList.isEmpty()) {
            return ResponseEntity.ok(resultAverageDTOList);
        }

        return ResponseEntity.notFound().build();
    }


    @GetMapping("/api/forecasts")
    public ResponseEntity<List<ViewForeCastDTO>> listAllForeCastFromDBWithDTO() throws IOException {
        //List<ViewForeCastDTO> resultOfAllList = userAdministrationServices.listAllForeCastFromDBWithDTO();  //original innan flytt
        List<ViewForeCastDTO> resultOfAllList = foreCastServices.listAllForeCastFromDBWithDTO();
        if (!resultOfAllList.isEmpty()) {
            return ResponseEntity.ok(resultOfAllList);
        }

        return ResponseEntity.notFound().build();
    }


    @PutMapping("/api/forecasts/{id}")
    public ResponseEntity<ViewForeCastDTO> update(@PathVariable UUID id, @RequestBody ViewForeCastDTO updatedForeCast) throws IOException {

        try {
            var foreCast = foreCastServices.get(id).get();
            foreCast.setDate(updatedForeCast.getDate());
            foreCast.setHour(updatedForeCast.getHour());
            foreCast.setTemperature(updatedForeCast.getTemperature());
            foreCast.setProvider(updatedForeCast.getProvider());
            foreCast.setPrecipitation(updatedForeCast.isPrecipitation());

            foreCastServices.updateWithDTO(foreCast);

            ViewForeCastDTO responseDTO = new ViewForeCastDTO(
                    updatedForeCast.getId(),
                    updatedForeCast.getDate(),
                    updatedForeCast.getHour(),
                    updatedForeCast.getTemperature(),
                    updatedForeCast.isPrecipitation(),
                    updatedForeCast.getProvider()
            );

            return ResponseEntity.ok(responseDTO);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }

    }





    //all info

    @PostMapping("/api/forecasts")
        //funkar via thunder
    ResponseEntity<ForeCast> addForeCast(@RequestBody ForeCast newForeCast) throws IOException {
        ForeCast createdForecast = foreCastServices.add(newForeCast);
        return new ResponseEntity<ForeCast>(createdForecast, HttpStatus.CREATED);
    }

    @GetMapping("/api/forecasts/{id}")
    public ResponseEntity<ForeCast> Get(@PathVariable UUID id) throws IOException {
        Optional<ForeCast> ForeCastSingle = foreCastServices.get(id);
        if (ForeCastSingle.isPresent()) return ResponseEntity.ok(ForeCastSingle.get());
        return ResponseEntity.notFound().build();
    }


    @DeleteMapping("/api/forecasts/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable UUID id) {
        foreCastServices.deleteWeather(id);
        return ResponseEntity.ok("Deleted");
    }

}
