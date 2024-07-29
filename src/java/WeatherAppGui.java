import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class WeatherAppGui extends HttpServlet {
   // private static final long serialVersionUID = 1L;
    private JSONObject weatherData;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        out.println("<html>");
        out.println("<head>");
        out.println("<title>Weather App</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; background-color: aliceblue; text-align: center; }");
        out.println("#search-container { margin-top: 20px; }");
        out.println("#weather-container { margin-top: 20px; }");
        out.println("#weather-info { background-color: #f0f0f0; border: 2px solid #4682b4; padding: 10px; margin-top: 10px; }");
        out.println("#weather-info img { max-width: 100px; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Weather App</h1>");
        out.println("<div id=\"search-container\">");
        out.println("<form action=\"WeatherAppGui\" method=\"post\">");//here the method is post so in WeatherAppGui do post method is called
        out.println("<input type=\"text\" name=\"location\" placeholder=\"Enter location\" required>");
        out.println("<button type=\"submit\">Search</button>");
        out.println("</form>");
        out.println("</div>");

        // Display weather information if available
        if (weatherData != null) {
            out.println("<div id=\"weather-container\">");
            out.println("<div id=\"weather-info\">");
            out.println("<h2>" + weatherData.get("weather_condition") + "</h2>");
            out.println("<img src=\"" + weatherData.get("weather_image") + "\" alt=\"" + weatherData.get("weather_condition") + "\">");
            out.println("<p>Temperature: " + weatherData.get("temperature") + " °C</p>");
            out.println("<p>Humidity: " + weatherData.get("humidity") + " %</p>");
            out.println("<p>Wind Speed: " + weatherData.get("windspeed") + " km/h</p>");
           // out.println("<p>Time: " + weatherData.get("time") + "</p>");
            out.println("</div>");
            out.println("</div>");
        } else {
            out.println("<p>No weather data available.</p>");
        }

        out.println("</body>");
        out.println("</html>");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String location = request.getParameter("location");//which is given in above input field

        if (location != null && !location.trim().isEmpty()) {
            weatherData = getWeatherData(location);
        }

        // Redirect to doGet to display updated weather information
        response.sendRedirect(request.getContextPath() + "/WeatherAppGui");
    }
//taking the data from json file
    private JSONObject getWeatherData(String locationName) {
        JSONArray locationData = getLocationData(locationName);

        if (locationData == null || locationData.isEmpty()) {
            System.out.println("Error: Location data not found.");
            return null;
        }

        try {
            JSONObject location = (JSONObject) locationData.get(0);
            double latitude = (double) location.get("latitude");
            double longitude = (double) location.get("longitude");

            String urlString = "https://api.open-meteo.com/v1/forecast?" +
                    "latitude=" + latitude + "&longitude=" + longitude +
                    "&hourly=temperature_2m,relativehumidity_2m,weathercode,windspeed_10m&timezone=America%2FLos_Angeles";

            HttpURLConnection conn = fetchApiResponse(urlString);
//Retrieves latitude and longitude from the location data, then constructs a URL for the weather API and fetches data using fetchApiResponse method.
            if (conn == null || conn.getResponseCode() != 200) {
                System.out.println("Error: Could not connect to API or received non-200 response");
                return null;
            }

            JSONObject resultJsonObj = parseResponse(conn.getInputStream());
//Parses the JSON response from the API using parseResponse method.
            if (resultJsonObj == null) {
                System.out.println("Error: Failed to parse API response");
                return null;
            }

            JSONObject hourly = (JSONObject) resultJsonObj.get("hourly");
            JSONArray time = (JSONArray) hourly.get("time");
            int index = findIndexOfCurrentTime(time);

            JSONArray temperatureData = (JSONArray) hourly.get("temperature_2m");
            double temperature = (double) temperatureData.get(index);

            JSONArray weathercode = (JSONArray) hourly.get("weathercode");
            String weatherCondition = convertWeatherCode((long) weathercode.get(index));

            JSONArray relativeHumidity = (JSONArray) hourly.get("relativehumidity_2m");
            long humidity = (long) relativeHumidity.get(index);

            JSONArray windspeedData = (JSONArray) hourly.get("windspeed_10m");
            double windspeed = (double) windspeedData.get(index);
//Extracts temperature, weather condition, humidity, and wind speed from the parsed JSON data.
            String currentTime = (String) time.get(index); // Assuming time is stored as a string
            String formattedTime = getCurrentTime(currentTime); // Format the API time

            JSONObject weatherData = new JSONObject();
            weatherData.put("temperature", temperature);
            weatherData.put("weather_condition", weatherCondition);
            weatherData.put("humidity", humidity);
            weatherData.put("windspeed", windspeed);
            weatherData.put("time", formattedTime); // Use formatted time
            weatherData.put("weather_image", getWeatherImage(weatherCondition)); // Custom method to get image URL
//Constructs a JSONObject (weatherData) to store all the retrieved weather information.
            return weatherData;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private JSONArray getLocationData(String locationName) {
        locationName = locationName.replaceAll(" ", "+");

        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" +
                locationName + "&count=10&language=en&format=json";
//getLocationData method retrieves location data (latitude, longitude) based on the provided location name.
        HttpURLConnection conn = fetchApiResponse(urlString);

        if (conn == null) {
            System.out.println("Error: Could not connect to geocoding API");
            return null;
        }

        try {
            JSONObject resultsJsonObj = parseResponse(conn.getInputStream());

            if (resultsJsonObj == null) {
                System.out.println("Error: Failed to parse geocoding API response");
                return null;
            } //Parses the JSON response from the geocoding API to retrieve location data.

            JSONArray locationData = (JSONArray) resultsJsonObj.get("results");
            return locationData;
             //Parses the JSON response from the geocoding API to retrieve location data.
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            conn.disconnect();
        }

        return null;
    }

    private HttpURLConnection fetchApiResponse(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            return conn;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
        //fetchApiResponse method establishes a connection to the API URL and returns a HttpURLConnection object.

    }

    private JSONObject parseResponse(InputStream inputStream) {
        try (Scanner scanner = new Scanner(inputStream)) {
            StringBuilder resultJson = new StringBuilder();
            while (scanner.hasNext()) {
                resultJson.append(scanner.nextLine());
            }
            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(resultJson.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private int findIndexOfCurrentTime(JSONArray timeList) {
        // Implement your logic to find the index of current time in the timeList
        // For simplicity, returning index 0 as a fallback
        return 0;
    }

    private String getCurrentTime(String apiTime) {
        LocalDateTime currentTime = LocalDateTime.parse(apiTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return currentTime.format(formatter);
    }

    private static String convertWeatherCode(long weathercode) {
        if (weathercode == 0L) {
            return "Clear";
        } else if (weathercode > 0L && weathercode <= 3L) {
            return "Cloudy";
        } else if ((weathercode >= 51L && weathercode <= 67L) ||
                (weathercode >= 80L && weathercode <= 99L)) {
            return "Rain";
        } else if (weathercode >= 71L && weathercode <= 77L) {
            return "Snow";
        } else {
            return "Unknown";
        }
    }

    private String getWeatherImage(String weatherCondition) {
        return switch (weatherCondition.toLowerCase()) {
            case "clear" -> "assets/clear.png";
            case "cloudy" -> "assets/cloudy.png";
            case "rain" -> "assets/rain.png";
            case "snow" -> "assets/snow.png";
            default -> "";
        };
    }
}
