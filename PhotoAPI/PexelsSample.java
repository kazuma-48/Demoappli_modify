package PhotoAPI;
import java.io.IOException;
import java.util.List;

public class PexelsSample {
    public static void main(String[] args){
        String apiKey = System.getenv("PEXELS_API_KEY"); // 環境変数から取得
        if(apiKey == null){
            System.out.println("環境変数が設定されていません。");
            System.out.println("環境変数PEXELS_API_KEYを設定してください。");
            return;
        }

        String query = "summer vacation";
        int perPage = 5;
        int page = 1;
        System.out.println("検索ワード: " + query);
        try{
            PexelsClient client = new PexelsClient(apiKey);
            List<Photo> photos = client.fetchPhotos(query, perPage, page);
            for(Photo photo : photos){
                System.out.println(String.format("・URL: %s",
                        photo.getOriginalUrl()));
            }
        } catch(IOException | java.net.URISyntaxException | org.json.JSONException e){
            e.printStackTrace();
        }
    }
}
