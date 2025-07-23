package QuizApp;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import org.json.*;

public class Cooking {

    // Spoonacular APIキーを入力してください
    private static final String API_KEY = "YOUR_SPOONACULAR_API_KEY";

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        // ランダムでレシピを取得
        int randomOffset = new Random().nextInt(100); // 0～99のランダムなオフセット
        String endpoint = "https://api.spoonacular.com/recipes/complexSearch?number=1&offset=" +
                randomOffset + "&apiKey=" + API_KEY;
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject result = new JSONObject(response.body());
        JSONArray recipes = result.getJSONArray("results");
        if (recipes.length() == 0) {
            System.out.println("レシピが見つかりませんでした。");
            return;
        }

        JSONObject recipe = recipes.getJSONObject(0);
        String title = recipe.getString("title");
        int id = recipe.getInt("id");

        // レシピの材料を取得
        String ingredientEndpoint = "https://api.spoonacular.com/recipes/" + id + "/ingredientWidget.json?apiKey=" + API_KEY;
        HttpRequest ingredientRequest = HttpRequest.newBuilder()
                .uri(URI.create(ingredientEndpoint))
                .build();
        HttpResponse<String> ingredientResponse = client.send(ingredientRequest, HttpResponse.BodyHandlers.ofString());
        JSONObject ingredientJson = new JSONObject(ingredientResponse.body());
        JSONArray ingredientsArray = ingredientJson.getJSONArray("ingredients");

        List<String> ingredientNames = new ArrayList<>();
        for (int i = 0; i < ingredientsArray.length(); i++) {
            ingredientNames.add(ingredientsArray.getJSONObject(i).getString("name"));
        }

        System.out.println("【クイズ】この食材で作れる料理はなに？");
        System.out.println("食材: " + String.join(", ", ingredientNames));
        System.out.print("答えを入力してください: ");
        String answer = scanner.nextLine().trim();

        if (answer.equalsIgnoreCase(title)) {
            System.out.println("正解！料理名: " + title);
        } else {
            System.out.println("不正解。正解は: " + title);
        }
    }
}
