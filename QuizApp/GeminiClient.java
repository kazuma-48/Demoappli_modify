package QuizApp;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;

/**
 * Gemini APIとの通信を担当するクラス
 * モデルバージョンは "gemini-2.5-flash" を使用しています。
 * 
 * @author n.katayama
 * @version 1.0
 */
public class GeminiClient {
    /**
     * Geminiに問い合わせる
     * 
     * @param question 質問内容
     * @param apikey APIキー
     * @return 回答
     */
    public static String queryGemini(String question, String apikey) throws Exception {
        String model = "gemini-2.5-flash";// 使用するモデルのバージョン
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/" +  model + ":generateContent?key="
                + apikey;
        String requestBody = """
                {
                    "contents": [
                        {
                            "parts": [
                                { "text": "%s"}
                            ]
                        }
                    ]
                }
                """.formatted(question);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject json = new JSONObject(response.body());
        String answer = json.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text");
        return answer;
    }
}