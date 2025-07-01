package PhotoAPI;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Pexels
 * 
 * 利用API:https://www.pexels.com/api/documentation/
 */
public class PexelsClient {
    private final String apiKey;

    public PexelsClient(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public List<Photo> fecthPhotos(String query, int page, int perPage) throws IOException, java.net.URISyntaxException,org.json.JSONException {
        String urlString = String.format("https://api.pexels.com/v1/search?query=%s&page=%d",query.replace(" ", "%20"), perPage, page);
        URL url = new URI(urlString).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", apiKey);
        int responseCode = connection.getResponseCode();
        if(responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("データの取得に失敗しました! レスポンスコード: " + responseCode);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        JSONObject json = new JSONObject(response.toString());
        JSONArray photos = json.optJSONArray("photos");
        List<Photo> photoList = new ArrayList<>();
        if (photos != null) {
            for (int i = 0; i < photos.length(); i++) {
                JSONObject photoObj = photos.getJSONObject(i);
                int photoId = photoObj.optInt("id");
                JSONObject src = photoObj.optJSONObject("src");
                String originalUrl = src != null ? src.optString("original") : null;
                photoList.add(new Photo(photoId, originalUrl));
            }
        }
        return photoList;
    }
}
