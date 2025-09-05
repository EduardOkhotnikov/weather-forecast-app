package com.weatherapp;

import com.weatherapp.model.*;
import com.weatherapp.service.WeatherService;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class WeatherApp {
    private static final String BASE_URL = "https://api.weatherapi.com/v1/";
    private static final String API_KEY = "";
    private static final List<String> CITIES = Arrays.asList("Chisinau", "Madrid", "Kyiv", "Amsterdam");

    public static void main(String[] args) {
        WeatherApp app = new WeatherApp();
        app.run();
    }

    public void run() {
        // Initialize Retrofit client with base URL and JSON converter
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherService weatherService = retrofit.create(WeatherService.class);

        System.out.println("Fetching weather forecast for tomorrow...\n");

        // Collect weather data for all cities
        Map<String, WeatherData> cityWeatherData = new HashMap<>();
        String tomorrowDate = "";
        boolean usingLocalData = false;
        for (String city : CITIES) {
            ForecastResponse forecast = null;

            try {
                // First try API
                forecast = getForecast(weatherService, city);
            } catch (IOException e) {
                System.out.println("API failed for " + city + ", trying local data...");
            }

            // If API failed, try local data
            if (forecast == null) {
                forecast = loadTestData();
                if (forecast != null && !usingLocalData) {
                    System.out.println("Using local test data as fallback...\n");
                    usingLocalData = true;
                }
            }

            if (forecast != null) {
                WeatherData weatherData = extractWeatherData(forecast);
                cityWeatherData.put(city, weatherData);
                if (tomorrowDate.isEmpty()) {
                    tomorrowDate = weatherData.getDate();
                }
            } else {
                System.out.println("No data available for " + city);
            }
        }

        if (!cityWeatherData.isEmpty()) {
            displayWeatherTable(cityWeatherData, tomorrowDate);
        } else {
            System.out.println("No weather data available from any source");

        }

        // Display collected data in table format
        if (!cityWeatherData.isEmpty()) {
            displayWeatherTable(cityWeatherData, tomorrowDate);
        } else {
            System.out.println("No weather data available");
        }
    }

    private ForecastResponse getForecast(WeatherService service, String city) throws IOException {
        Call<ForecastResponse> call = service.getForecast(API_KEY, city, 2); // 2 дня = tomorrow
        Response<ForecastResponse> response = call.execute();

        if (response.isSuccessful()) {
            return response.body();
        } else {
            System.err.println("API Error for " + city + ": " + response.code() + " " + response.message());
            return null;
        }
    }


    private WeatherData extractWeatherData(ForecastResponse forecast) {
        var tomorrowForecast = forecast.getForecast().getForecastday().get(1);
        var dayData = tomorrowForecast.getDay();

        // Get wind direction in 12:00 (index 12)
        String windDirection = "N/A";
        if (tomorrowForecast.getHour() != null && tomorrowForecast.getHour().size() > 12) {
            windDirection = tomorrowForecast.getHour().get(12).getWind_dir();
        }

        return new WeatherData(
                tomorrowForecast.getDate(),
                dayData.getMintemp_c(),
                dayData.getMaxtemp_c(),
                dayData.getAvghumidity(),
                dayData.getMaxwind_kph(),
                windDirection
        );
    }

    private void displayWeatherTable(Map<String, WeatherData> cityData, String date) {
        System.out.println("Weather Forecast for " + date);
        System.out.println();

        // Headline table
        System.out.printf("%-12s | %-8s | %-8s | %-10s | %-10s | %-8s%n",
                "City", "Min°C", "Max°C", "Humidity%", "Wind kph", "Wind Dir");
        System.out.println("-------------|----------|----------|------------|------------|----------");

        // Data of cities
        for (String city : CITIES) {
            WeatherData data = cityData.get(city);
            if (data != null) {
                System.out.printf("%-12s | %-8.1f | %-8.1f | %-10.0f | %-10.1f | %-8s%n",
                        city,
                        data.getMinTemp(),
                        data.getMaxTemp(),
                        data.getHumidity(),
                        data.getWindSpeed(),
                        data.getWindDirection()
                );
            } else {
                System.out.printf("%-12s | %-8s | %-8s | %-10s | %-10s | %-8s%n",
                        city, "N/A", "N/A", "N/A", "N/A", "N/A");
            }
        }
    }

    // Inner class for storage weather data
    private static class WeatherData {
        private final String date;
        private final double minTemp;
        private final double maxTemp;
        private final double humidity;
        private final double windSpeed;
        private final String windDirection;

        public WeatherData(String date, double minTemp, double maxTemp,
                           double humidity, double windSpeed, String windDirection) {
            this.date = date;
            this.minTemp = minTemp;
            this.maxTemp = maxTemp;
            this.humidity = humidity;
            this.windSpeed = windSpeed;
            this.windDirection = windDirection;
        }

        public String getDate() { return date; }
        public double getMinTemp() { return minTemp; }
        public double getMaxTemp() { return maxTemp; }
        public double getHumidity() { return humidity; }
        public double getWindSpeed() { return windSpeed; }
        public String getWindDirection() { return windDirection; }
    }
    private ForecastResponse loadTestData() {
        try {
            // Calculate tomorrow's date
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            String tomorrowStr = tomorrow.toString();

            // Create mock data with current dates
            ForecastResponse response = new ForecastResponse();
            Forecast forecast = new Forecast();

            // Create today and tomorrow forecast days
            ForecastDay today = createMockForecastDay(LocalDate.now().toString());
            ForecastDay tomorrowDay = createMockForecastDay(tomorrowStr);

            forecast.setForecastday(Arrays.asList(today, tomorrowDay));
            response.setForecast(forecast);

            return response;

        } catch (Exception e) {
            System.err.println("Error creating test data: " + e.getMessage());
            return null;
        }
    }
    private ForecastDay createMockForecastDay(String date) {
        Random random = new Random();

        ForecastDay forecastDay = new ForecastDay();
        forecastDay.setDate(date);

        // Create mock day data with more variation
        Day day = new Day();
        day.setMintemp_c(12.0 + random.nextDouble() * 10);
        day.setMaxtemp_c(20.0 + random.nextDouble() * 15);
        day.setAvghumidity(50 + random.nextDouble() * 30);
        day.setMaxwind_kph(10 + random.nextDouble() * 20);
        forecastDay.setDay(day);

        // Create hourly data with random wind direction for each city
        List<Hour> hours = new ArrayList<>();
        String[] directions = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};

        for (int i = 0; i < 24; i++) {
            Hour hour = new Hour();
            hour.setTime(date + " " + String.format("%02d:00", i));
            // Random wind direction for each hour
            hour.setWind_dir(directions[random.nextInt(directions.length)]);
            hours.add(hour);
        }
        forecastDay.setHour(hours);

        return forecastDay;
    }
}
