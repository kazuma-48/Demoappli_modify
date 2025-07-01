import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Spotify
 * 
 * 利用API: https://developer.spotify.com/documentation/web-api/
 */
public class SpotifyClient{
    private String clientId;
    private String clientSecret;
    private String accessToken;

    public SpotifyClient(String clientId, String clientSecret) throws Exception {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.accessToken = getAccessToken();
    }

    private String getAccessToken() throws Exception {
        URI uri = new URI("https://acccounts.spotify.com/api/token");
        URL url = uri.toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        String params = "grant_type=client_credentials"
                + "&client_id=" + URLEncoder.encode(clientId,"UTF-8")
                + "&client_secret=" + URLEncoder.encode(clientSecret, "UTF-8");
        
        try(OutputStream os = conn.getOutputStream()){
            os.write(params.getBytes(StandardCharsets.UTF_8));
        }

        String response = readResponse(conn);
        JSONObject json = new JSONObject(response);
        return json.getString("access_token");
    }

    public List<String> getArtistSongs(String artist, int limit) throws Exception {
        String urlStr = "https://api.spotify.com/v1/search?q=" + URLEncoder.encode("artist:" + artist, "UTF-8")
                + "&type=track&limit=" + limit;
        URI uri = new URI(urlStr);
        URL url = uri.toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);

        String response = readResponse(conn);
        JSONObject json = new JSONObject(response);

        List<String> items = new ArrayList<>();
        if(json.has("tracks")){
            JSONArray tracks = json.getJSONObject("tracks").getJSONArray("items");
            for(int i = 0; i < tracks.length(); i++){
                items.add(tracks.getJSONObject(i).getString("name"));
            }
        }
        return items;
    }

    public List<String> getAlbumInfo(String artist, String title) throws Exception{
        String urlStr = "https://api.spotify.com/v1/search?q="
                + URLEncoder.encode("artist:" + artist + "track:" + title, "UTF-8")
                + "&type=track&limit=1";
        URI uri = new URI(urlStr);
        URL url = uri.toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);

        String response = readResponse(conn);
        JSONObject json = new JSONObject(response);

        List<String> items = new ArrayList<>();
        if(json.has("tracks")){
            JSONArray tracks = json.getJSONObject("tracks").getJSONArray("items");
            if(tracks.length() > 0){
                JSONObject album = tracks.getJSONObject(0).getJSONObject("album");
                items.add(album.getString("name"));
                items.add(album.getJSONArray("images").getJSONObject(0).getString("url"));
            }
        }
        return items;
    }

    private static String readResponse(HttpURLConnection conn)throws IOException{
        try(BufferedReader in = new BufferedReader(
            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))){
            StringBuilder sb = new StringBuilder();
            String line;
            while((line = in.readLine()) != null)
                sb.append(line);
                return sb.toString();
        }
    }
}


