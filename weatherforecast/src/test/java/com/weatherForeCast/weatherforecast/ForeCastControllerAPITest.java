package com.weatherForeCast.weatherforecast;


import com.weatherForeCast.weatherforecast.controller.ForeCastControllerAPI;
import com.weatherForeCast.weatherforecast.dto.AverageDTO;
import com.weatherForeCast.weatherforecast.provider.WeatherProvider;
import com.weatherForeCast.weatherforecast.services.ForeCastServices;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;



import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;

import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@ExtendWith(MockitoExtension.class)
public class ForeCastControllerAPITest {

    // Mocking the service that the controller depends on
    @Mock
    private ForeCastServices foreCastServices;

    // Injecting the mock services into the controller
    @InjectMocks
    private ForeCastControllerAPI foreCastControllerAPI;

    // Setting up the MockMvc instance to test the controller
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(foreCastControllerAPI).build();
    }

    // Testing the getAverageByDateDTO method in the controller
    @Test
    public void testGetAverageDTO() throws Exception {
        // Arrange
        LocalDate selectedDate = LocalDate.now();
        System.out.println("Testing getAverageDTO for selectedDate: " + selectedDate);
        // Mocking the service response
        when(foreCastServices.getAverageByDateDTO(selectedDate))
                .thenReturn(Collections.singletonList(new AverageDTO(selectedDate, 20.0f, LocalTime.now())));

        // Act & Assert
        // Performing a GET request to the controller endpoint with the selectedDate as a path variable
        // Expecting a JSON response with a status code of 200 (OK)
        // Also, verifying that the service method was called with the correct arguments
        System.out.println("Performing a GET request for getAverageDTO endpoint...");
        this.mockMvc.perform(get("/api/forecasts/average/{selectedDate}", selectedDate)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].averageTemperature").value(20.0));

        // Verify
        // Verifying that the service method was called exactly once with the selectedDate argument
        verify(foreCastServices, times(1)).getAverageByDateDTO(selectedDate);
        System.out.println("getAverageDTO test completed successfully.");
    }

    // Testing the getAverageFromWhatEverIsTypedInDTO method in the controller
    @Test
    public void testGetAverageFromWhatEverIsTypedInDTO() throws Exception {
        // Arrange
        LocalDate selectedDate = LocalDate.now();
        String providerString = "SMHI";
        WeatherProvider provider = WeatherProvider.SMHI;
        System.out.println("Testing getAverageFromWhatEverIsTypedInDTO for selectedDate: " + selectedDate + " and provider: " + providerString);
        // Mocking the service response
        when(foreCastServices.getAverageFromWhatEverIsTypedInDTO(selectedDate, provider))
                .thenReturn(Collections.singletonList(new AverageDTO(selectedDate, 22.0f, LocalTime.now())));

        // Act & Assert
        // Performing a GET request to the controller endpoint with selectedProvider and selectedDate as path variables
        // Expecting a JSON response with a status code of 200 (OK)
        // Also, verifying that the service method was called with the correct arguments
        mockMvc.perform(get("/api/forecasts/average/{selectedProvider}/{selectedDate}", providerString, selectedDate)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].averageTemperature").value(22.0));

        // Verify
        // Verifying that the service method was called exactly once with the selectedDate and provider arguments
        verify(foreCastServices, times(1)).getAverageFromWhatEverIsTypedInDTO(selectedDate, provider);
        System.out.println("getAverageFromWhatEverIsTypedInDTO test completed successfully.");
    }
    }

    // Additional comments: Add similar tests for other controller methods (listAllForeCastFromDBWithDTO, update, add, Get, deleteProduct)

