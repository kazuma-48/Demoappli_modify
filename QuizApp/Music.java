package QuizApp;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import org.json.*;

public class Music {

    // 楽曲情報クラス
    public static class MusicInfo {
        private String title;
        private String artist;
        private String album;
        private List<String> genres;

        public MusicInfo(String title, String artist, String album, List<String> genres) {
            this.title = title;
            this.artist = artist;
            this.album = album;
            this.genres = genres;
        }

        public String getTitle() { return title; }
        public String getArtist() { return artist; }
        public String getAlbum() { return album; }
        public List<String> getGenres() { return genres; }

        @Override
        public String toString() {
            return "タイトル: " + title + "\nアーティスト: " + artist + "\nアルバム: " + album + "\nジャンル: " + genres;
        }
    }

    // Spotify APIからランダムな楽曲を取得
    public static MusicInfo getRandomMusic(String accessToken) throws Exception {
        // Spotifyの"Browse"カテゴリから人気プレイリストを取得
        String playlistEndpoint = "https://api.spotify.com/v1/browse/featured-playlists?country=JP&limit=10";
        HttpRequest playlistRequest = HttpRequest.newBuilder()
                .uri(URI.create(playlistEndpoint))
                .header("Authorization", "Bearer " + accessToken)
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> playlistResponse = client.send(playlistRequest, HttpResponse.BodyHandlers.ofString());
        JSONObject playlistsJson = new JSONObject(playlistResponse.body());
        JSONArray playlists = playlistsJson.getJSONObject("playlists").getJSONArray("items");

        // ランダムなプレイリストを選択
        JSONObject playlist = playlists.getJSONObject(new Random().nextInt(playlists.length()));
        String playlistId = playlist.getString("id");

        // プレイリストのトラックを取得
        String tracksEndpoint = "https://api.spotify.com/v1/playlists/" + playlistId + "/tracks?limit=50";
        HttpRequest tracksRequest = HttpRequest.newBuilder()
                .uri(URI.create(tracksEndpoint))
                .header("Authorization", "Bearer " + accessToken)
                .build();
        HttpResponse<String> tracksResponse = client.send(tracksRequest, HttpResponse.BodyHandlers.ofString());
        JSONObject tracksJson = new JSONObject(tracksResponse.body());
        JSONArray tracks = tracksJson.getJSONArray("items");

        // ランダムなトラックを選択
        JSONObject trackObj = tracks.getJSONObject(new Random().nextInt(tracks.length())).getJSONObject("track");
        String title = trackObj.getString("name");
        String album = trackObj.getJSONObject("album").getString("name");
        JSONArray artistsArray = trackObj.getJSONArray("artists");
        String artist = artistsArray.getJSONObject(0).getString("name");
        String artistId = artistsArray.getJSONObject(0).getString("id");

        // アーティストのジャンルを取得
        String artistEndpoint = "https://api.spotify.com/v1/artists/" + artistId;
        HttpRequest artistRequest = HttpRequest.newBuilder()
                .uri(URI.create(artistEndpoint))
                .header("Authorization", "Bearer " + accessToken)
                .build();
        HttpResponse<String> artistResponse = client.send(artistRequest, HttpResponse.BodyHandlers.ofString());
        JSONObject artistJson = new JSONObject(artistResponse.body());
        JSONArray genresArray = artistJson.getJSONArray("genres");
        List<String> genres = new ArrayList<>();
        for (int i = 0; i < genresArray.length(); i++) {
            genres.add(genresArray.getString(i));
        }

        return new MusicInfo(title, artist, album, genres);
    }

    // クイズを出題
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Spotifyのアクセストークンを入力してください: ");
        String accessToken = scanner.nextLine().trim();

        MusicInfo music = getRandomMusic(accessToken);

        System.out.println("【Spotifyクイズ】");
        System.out.println("この曲のアーティスト名は？");
        System.out.println("タイトル: " + music.getTitle());
        System.out.println("アルバム: " + music.getAlbum());
        System.out.print("答え: ");
        String answer = scanner.nextLine().trim();

        if (answer.equalsIgnoreCase(music.getArtist())) {
            System.out.println("正解！");
        } else {
            System.out.println("不正解。正解は: " + music.getArtist());
        }
    }
}