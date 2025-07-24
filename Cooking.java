

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import org.json.*;

public class Cooking {
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

    public static Quiz getQuiz() {
        // TheMealDB APIでランダムレシピ取得
        String endpoint = "https://www.themealdb.com/api/json/v1/1/random.php";
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject result = new JSONObject(response.body());
            JSONArray meals = result.optJSONArray("meals");
            if (meals == null || meals.length() == 0) {
                return new Quiz("レシピが見つかりませんでした。", new String[] { "-", "-", "-", "-" }, 0);
            }
            JSONObject meal = meals.getJSONObject(0);
            String title = meal.optString("strMeal", "料理名不明");
            // 具材抽出（strIngredient1～strIngredient20）
            List<String> ingredientNames = new ArrayList<>();
            for (int i = 1; i <= 20; i++) {
                String ing = meal.optString("strIngredient" + i, "").trim();
                if (!ing.isEmpty())
                    ingredientNames.add(ing);
            }
            // 正解以外の選択肢をGemini APIで正解と同じ国の料理名にする
            String titleJa = GeminiClient.translate("次の料理名を日本語に翻訳してください: " + title + " そして、解説文を出力しないでください。");
            // Geminiで同じ国の料理名を3つ取得
            String prompt = "「" + titleJa + "」と同じ国の料理名を3つ挙げてください。料理名のみ日本語で単語で出力してください。また、解説文は出力しないでください。";
            String aiDishes = GeminiClient.translate(prompt);
            // 箇条書き（\n区切り）で分割
            List<String> dishList = new ArrayList<>();
            for (String line : aiDishes.split("\n")) {
                String dish = line.replaceAll("^[-・●\\d.\\s]*", "").trim();
                if (!dish.isEmpty() && !dish.equals(titleJa)) {
                    dishList.add(dish);
                }
            }
            // 選択肢（正解＋AI生成3つのみ、重複なし）
            List<String> choicesList = new ArrayList<>();
            for (String dish : dishList) {
                if (!choicesList.contains(dish) && choicesList.size() < 3) {
                    choicesList.add(dish);
                }
            }
            // 4つ未満ならダミーで埋める（重複しないように）
            while (choicesList.size() < 3) {
                String dummy = "料理名不明";
                int suffix = 1;
                while (choicesList.contains(dummy)) {
                    dummy = "料理名不明" + (suffix++);
                }
                choicesList.add(dummy);
            }
            // 正解をランダムな位置に挿入
            Random rand = new Random();
            int correctIdxJa = rand.nextInt(4);
            choicesList.add(correctIdxJa, titleJa);
            String[] choices = choicesList.toArray(new String[0]);
            // 問題文は食材情報のみ（選択肢は含めない）
            String quizText = GeminiClient.translate(
                    "次の文章を日本語でクイズ問題文として自然に表示してください。また、問題文の食材は箇条書きにしてください。(承知しました等の確認、*は出力しないでください。): 食材： " + String.join(", ", ingredientNames));
            return new Quiz(quizText, choices, correctIdxJa);
        } catch (Exception e) {
            // API通信やJSONパース失敗時
            return new Quiz("エラーが発生しました: " + e.getMessage(), new String[] { "-", "-", "-", "-" }, 0);
        }
    }
}
