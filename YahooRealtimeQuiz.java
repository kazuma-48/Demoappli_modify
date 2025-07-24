import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.*;
import java.util.*;

public class YahooRealtimeQuiz {

    // Yahoo!リアルタイム検索APIのアプリケーションIDを入力してください
    private static final String APP_ID = "YOUR_YAHOO_APP_ID";

    /**
     * Yahoo!リアルタイム検索APIからランダムなトレンドキーワードを取得します
     * @return 話題のキーワード（取得できない場合はnull）
     */
    public static String getRandomTrendKeyword() {
        String endpoint = "https://api.yahoo.co.jp/RealtimeSearchService/V1/trend?appid=" + APP_ID + "&output=json";
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject json = new JSONObject(response.body());
            JSONArray keywords = json.getJSONObject("ResultSet").getJSONObject("Result").getJSONArray("Keyword");

            if (keywords.length() == 0) {
                return null;
            }

            JSONObject keywordObj = keywords.getJSONObject(new Random().nextInt(keywords.length()));
            return keywordObj.getString("Query");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Yahoo!リアルタイム検索APIから取得したキーワードで選択肢付きクイズを作成
     * @return クイズ文・選択肢・正解インデックス
     */
    public static Quiz getTrendKeywordQuiz() {
        String endpoint = "https://api.yahoo.co.jp/RealtimeSearchService/V1/trend?appid=" + APP_ID + "&output=json";
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject json = new JSONObject(response.body());
            JSONArray keywords = json.getJSONObject("ResultSet").getJSONObject("Result").getJSONArray("Keyword");

            if (keywords.length() < 4) {
                return new Quiz("キーワードが取得できませんでした。", new String[] { "-", "-", "-", "-" }, 0);
            }

            // 4つランダムに選択肢を作成
            List<String> keywordList = new ArrayList<>();
            for (int i = 0; i < keywords.length(); i++) {
                keywordList.add(keywords.getJSONObject(i).getString("Query"));
            }
            Collections.shuffle(keywordList);
            List<String> choices = keywordList.subList(0, 4);
            int correctIdx = new Random().nextInt(4);
            String correctKeyword = choices.get(correctIdx);

            String question = "【Yahoo!リアルタイム検索クイズ】今話題のキーワードはどれ？\n選択肢:";
            for (int i = 0; i < choices.size(); i++) {
                question += "\n" + (i + 1) + ". " + choices.get(i);
            }

            return new Quiz(question, choices.toArray(new String[0]), correctIdx);
        } catch (Exception e) {
            return new Quiz("キーワードが取得できませんでした。", new String[] { "-", "-", "-", "-" }, 0);
        }
    }

    // クイズ情報を保持するクラス
    public static class Quiz {
        public final String question;
        public final String[] choices;
        public final int correctIdx;

        public Quiz(String question, String[] choices, int correctIdx) {
            this.question = question;
            this.choices = choices;
            this.correctIdx = correctIdx;
        }
    }

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        // Yahoo!リアルタイム検索APIからトレンドキーワード取得
        String keyword = getRandomTrendKeyword();

        if (keyword == null) {
            System.out.println("キーワードが取得できませんでした。");
            return;
        }

        // クイズ出題
        System.out.println("【Yahoo!リアルタイム検索クイズ】今話題のキーワードは何でしょう？");
        System.out.println("ヒント: Twitterなどで急上昇中のワードです。");
        System.out.print("答えを入力してください: ");
        String answer = scanner.nextLine().trim();

        if (answer.equalsIgnoreCase(keyword)) {
            System.out.println("正解！キーワード: " + keyword);
        } else {
            System.out.println("不正解。正解は: " + keyword);
        }
    }
}
