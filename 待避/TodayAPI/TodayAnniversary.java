package 待避.TodayAPI;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.json.JSONObject;

/**
 * 今日は何の日？
 * 
 * 利用API: https://www.whatistoday.cyou/index.cgi/
 */
public class TodayAnniversary {
    private final String baseUrl = "https://api.whatistoday.cyou/v3/anniv/";

    public String getTodayAnniversary() throws IOException, java.net.URISyntaxException,org.json.JSONException{
        String mmdd = LocalDate.now().format(DateTimeFormatter.ofPattern("MMdd"));
        String urlString = baseUrl + mmdd;
        URL url = new URI(urlString).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        int responseCode = connection.getResponseCode();
        if(responseCode != HttpURLConnection.HTTP_OK){
            throw new IOException("データの取得に失敗しました！ レスポンスコード: " + responseCode);
        }
        StringBuilder responseBody = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(connection.getInputStream(),"UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                responseBody.append(line);
            }
        }

        JSONObject json = new JSONObject(responseBody.toString());
        return json.getString("anniv1");
    }
}
