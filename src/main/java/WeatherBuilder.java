import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by Kanes on 26.01.2017.
 */
public class WeatherBuilder {

    private String key = "";

    public String getWeather() {
        try {
            String json = readUrl("http://api.openweathermap.org/data/2.5/weather?&" +
                    "id=3094802&units=metric&APPID=" + key);
            Gson gson = new Gson();
            WeatherData weatherData = gson.fromJson(json, WeatherData.class);
            return "In Crocow the weather is " + weatherData.weather[0].main + ", " + weatherData.main.temp + "°C, "
                    + weatherData.main.pressure + "hPa, humidity: " + weatherData.main.humidity + "%";
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private String readUrl(String surl) throws Exception {
        BufferedReader reader = null;
        try {
            URL url = new URL(surl);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int ch;
            while ((ch = reader.read()) != -1)
                buffer.append((char) ch);
            return buffer.toString();
        } finally {
            if(reader != null)
                reader.close();
        }
    }



}
