package com.weatherForeCast.weatherforecast.repository;

import com.weatherForeCast.weatherforecast.MainModule.ForeCast;
import com.weatherForeCast.weatherforecast.provider.WeatherProvider;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface ForecastRepository extends CrudRepository <ForeCast, UUID>{


    @Override
    List<ForeCast> findAll();
   void deleteById(UUID forecastId);

    List<ForeCast> findByDate(LocalDate date);
    List<ForeCast> findByDateAndProvider(LocalDate date, WeatherProvider provider);



    @Query("SELECT f FROM ForeCast f ORDER BY f.date, f.hour")
    List<ForeCast> findAllOrdered();



/*  Denna querar från nu->24hframåt från databasen men jag stoppar in materialet med den logiken
    @Query("SELECT f FROM ForeCast f WHERE f.date >= :currentDate ORDER BY f.date, f.hour")
    List<ForeCast> findAllOrdered(@Param("currentDate") LocalDate currentDate);
*/

    @Modifying
    @Transactional
    @Query("UPDATE ForeCast f SET f.date = :newDate WHERE f.id = :forecastId")
    void updateDateById(@Param("forecastId") UUID forecastId, @Param("newDate") LocalDate newDate);

    @Modifying
    @Transactional
    @Query("UPDATE ForeCast f SET f.hour = :newHour WHERE f.id = :forecastId")
    void updateTimeById(@Param("forecastId") UUID forecastId, @Param("newHour") LocalTime newHour);


    @Modifying
    @Transactional
    @Query("UPDATE ForeCast f SET f.temperature = :newTemperature WHERE f.id = :forecastId")
    void updateTemperatureById(@Param("forecastId") UUID forecastId, @Param("newTemperature") float newTemperature);




}
