package MusicAPI;
import java.util.List;

public class SpotifySample {
    public static void main(String[] args) throws Exception {
        String clientID = System.getenv("CLIENT_ID"); //環境変数から取得
        String clientSecret = System.getenv("CLIENT_SECRET"); //環境変数から取得
        if(clientID == null || clientSecret == null) {
            System.out.println("環境変数が設定されていません。");
            System.out.println("環境変数 CLIENT_ID,CLIENT_SECRETを設定してください。");
            return;
        }

        SpotifyClient spotifyClient = new SpotifyClient(clientID, clientSecret);

        List<String> songs = spotifyClient.getArtistSongs("YOASOBI,10");
        System.out.println(songs);

        List<String> albumInfo = spotifyClient.getAlbumInfo("YOASOBI","群青");
        System.out.println(albumInfo);
    }
}