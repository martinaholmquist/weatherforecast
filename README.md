# weatherforecast

This project is a Weather Forecast application built using Spring Boot. 
It provides a RESTful API for retrieving, adding, updating, and deleting weather forecasts. 
The application stores weather forecasts in a MySQL database and supports multiple weather providers, including SMHI, MET, and user-generated forecasts.
 

The application offers the following features:

1. **Add Weather Forecast from SMHI**: Retrieves weather forecasts from SMHI and saves them to the database.
2. **Add Weather Forecast from MET**: Retrieves weather forecasts from MET and saves them to the database.
3. **View All Weather Forecasts**: Displays a list of all stored weather forecasts.
4. **Create a Custom Weather Forecast**: Allows users to create a custom weather forecast and save it to the database.
5. **Update a Weather Forecast**: Allows users to update an existing weather forecast.
6. **Delete a Weather Forecast**: Enables the removal of a weather forecast from the database.
7. **Get Average Temperature**: Calculates and displays the average temperature for a selected day. You can retrieve the average temperature for a specific date by using the following endpoints:
    - `GET /api/average/{selectedDate}`: Retrieves the average temperature for the selected date from all weather forecast providers in the database.
    - `GET /api/average/{selectedProvider}/{selectedDate}`: Retrieves the average temperature for the selected date from a specific weather forecast provider (e.g., SMHI, MET) in the database.
   The average temperature is calculated based on the available data for the selected date and is displayed with hourly granularity.


## API Documentation

The project provides an API with the following endpoints:

- `GET /api/average/{selectedDate}`: Retrieves the average temperature for a given day.
- `GET /api/average/{selectedProvider}/{selectedDate}`: Retrieves the average temperature from a specific weather forecast provider for a particular day.
- `GET /api/forecasts`: Retrieves a list of all weather forecasts.
- `PUT /api/forecasts/{id}`: Updates a weather forecast with the specified ID.
- `POST /api/forecasts`: Creates a new weather forecast.
- `GET /api/forecasts/{id}`: Retrieves an individual weather forecast with the specified ID.
- `DELETE /api/forecasts/{id}`: Deletes a weather forecast with the specified ID.

For more information and detailed usage instructions, please refer to the [Swagger documentation](http://localhost:8080/swagger-ui/index.html) when running the application locally.
