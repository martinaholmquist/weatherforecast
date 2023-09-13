package com.weatherForeCast.weatherforecast.controller;

import com.weatherForeCast.weatherforecast.MainModule.ForeCast;
import com.weatherForeCast.weatherforecast.dto.AverageDTO;
import com.weatherForeCast.weatherforecast.dto.ViewForeCastDTO;
import com.weatherForeCast.weatherforecast.provider.WeatherProvider;
import com.weatherForeCast.weatherforecast.services.ForeCastServices;
import com.weatherForeCast.weatherforecast.services.userAdministrationServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@RestController
public class ForeCastControllerAPI {

    @Autowired
    ForeCastServices foreCastServices;

    @Autowired
    userAdministrationServices userAdministrationServices;


    //JOBBAR MED DTO
    @GetMapping("/api/average/{selectedDate}")
    public ResponseEntity<List<AverageDTO>> getAverageDTO(@PathVariable("selectedDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate selectedDate) throws IOException {
       // List<AverageDTO> resultAverageDTOList = userAdministrationServices.getAverageByDateDTO(selectedDate);  //ORIGINAL INNAN FLYTT
        List<AverageDTO> resultAverageDTOList = foreCastServices.getAverageByDateDTO(selectedDate);

        if (!resultAverageDTOList.isEmpty()) {
            return ResponseEntity.ok(resultAverageDTOList);
        }

        return ResponseEntity.notFound().build();
    }


    @GetMapping("/api/average/{selectedProvider}/{selectedDate}")
    public ResponseEntity<List<AverageDTO>> getAverageFromWhatEverIsTypedInDTO(
            @PathVariable("selectedProvider") String providerString,
            @PathVariable("selectedDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate selectedDate) throws IOException {

        WeatherProvider provider = WeatherProvider.valueOf(providerString.toUpperCase());
        //List<AverageDTO> resultAverageDTOList = userAdministrationServices.getAverageFromWhatEverIsTypedInDTO(selectedDate, provider);  //original innan flytt
        List<AverageDTO> resultAverageDTOList = foreCastServices.getAverageFromWhatEverIsTypedInDTO(selectedDate, provider);

        if (!resultAverageDTOList.isEmpty()) {
            return ResponseEntity.ok(resultAverageDTOList);
        }

        return ResponseEntity.notFound().build();
    }


    //GET med DTO
    @GetMapping("/api/forecasts")
    public ResponseEntity<List<ViewForeCastDTO>> listAllForeCastFromDBWithDTO() throws IOException {
        //List<ViewForeCastDTO> resultOfAllList = userAdministrationServices.listAllForeCastFromDBWithDTO();  //original innan flytt
        List<ViewForeCastDTO> resultOfAllList = foreCastServices.listAllForeCastFromDBWithDTO();
        if (!resultOfAllList.isEmpty()) {
            return ResponseEntity.ok(resultOfAllList);
        }

        return ResponseEntity.notFound().build();
    }


    //PUT med DTO
    @PutMapping("/api/forecasts/{id}")
    public ResponseEntity<ViewForeCastDTO> update(@PathVariable UUID id, @RequestBody ViewForeCastDTO updatedForeCast) throws IOException {
        var foreCast = new ForeCast();
        foreCast.setId(id);
        foreCast.setDate(updatedForeCast.getDate());
        foreCast.setHour(updatedForeCast.getHour());
        foreCast.setTemperature(updatedForeCast.getTemperature());
        foreCast.setProvider(updatedForeCast.getProvider());
        foreCast.setPrecipitation(updatedForeCast.isPrecipitation());

        foreCastServices.updateWithDTO(foreCast);


        ViewForeCastDTO responseDTO = new ViewForeCastDTO(
                updatedForeCast.getDate(),
                updatedForeCast.getHour(),
                updatedForeCast.getTemperature(),
                updatedForeCast.isPrecipitation(),
                updatedForeCast.getProvider()

        );

        return ResponseEntity.ok(responseDTO);

    }



/*  testar med DTO på denna istället
    @GetMapping("/api/forecasts") //funkar via thunder
    public ResponseEntity<List<ForeCast>> GetAll() throws IOException {
        return new ResponseEntity<>(forecastRepositorytoAPI.findAllOrdered(), HttpStatus.OK);
    }*/


    //övriga

    @PostMapping("/api/forecasts")
        //funkar via thunder
    ResponseEntity<ForeCast> addForeCast(@RequestBody ForeCast newForeCast) throws IOException {
        ForeCast createdForecast = foreCastServices.add(newForeCast);
        return new ResponseEntity<ForeCast>(createdForecast, HttpStatus.CREATED);
    }

    @GetMapping("/api/forecasts/{id}")  //Funkar via thunder   Notera formatet på ID i urlen i thunderclient
    public ResponseEntity<ForeCast> Get(@PathVariable UUID id) throws IOException {
        Optional<ForeCast> ForeCastSingle = foreCastServices.get(id);
        if (ForeCastSingle.isPresent()) return ResponseEntity.ok(ForeCastSingle.get());
        return ResponseEntity.notFound().build();
    }


/*   original
    @PutMapping("/api/forecasts/{id}")  //Funkar via thunder   Notera formatet på ID i urlen i thunderclient
    public ResponseEntity<ForeCast> Update(@PathVariable UUID id, @RequestBody ForeCast ForeCastSingle) throws IOException {
        foreCastServices.update(ForeCastSingle);
        return ResponseEntity.ok(ForeCastSingle);
    }*/





    @DeleteMapping("/api/forecasts/{id}")
    //denna bör uppdateras med en typ isActive or not!!! Funkar via thunder   Notera formatet på ID i urlen i thunderclient http://localhost:8080/api/forecasts/6cb22a7f-c9fd-4f69-9d6d-fe55dc40f2ab
    public ResponseEntity<String> deleteProduct(@PathVariable UUID id) throws IOException {
        foreCastServices.deleteWeather(id);
        return ResponseEntity.ok("Deleted");
    }

}
