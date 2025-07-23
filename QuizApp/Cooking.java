package QuizApp;

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
            // ダミー選択肢（例: 料理名をランダム生成）
            List<String> choicesList = new ArrayList<>();
            choicesList.add(title);
            String[] dummyChoices = { "カレーライス", "ハンバーグ", "オムライス", "肉じゃが", "親子丼", "天ぷら", "すき焼き", "ラーメン" };
            Random rand = new Random();
            while (choicesList.size() < 4) {
                String dummy = dummyChoices[rand.nextInt(dummyChoices.length)];
                if (!choicesList.contains(dummy)) {
                    choicesList.add(dummy);
                }
            }
            Collections.shuffle(choicesList);
            String[] choices = choicesList.toArray(new String[0]);
            int correctIdx = -1;
            for (int i = 0; i < choices.length; i++) {
                if (choices[i].equals(title)) {
                    correctIdx = i;
                    break;
                }
            }
            // Geminiでクイズ形式の日本語に変換
            StringBuilder quizPrompt = new StringBuilder();
            quizPrompt.append(String.join(", ", ingredientNames) + "\n");
            quizPrompt.append("選択肢:\n");
            for (int i = 0; i < choices.length; i++) {
                quizPrompt.append((i + 1) + ". " + choices[i] + "\n");
            }
            String quizText = GeminiClient.translate("次の文章を日本語のクイズ形式で自然に表示してください: " + quizPrompt.toString());
            String[] choicesJa = new String[choices.length];
            for (int i = 0; i < choices.length; i++) {
                choicesJa[i] = GeminiClient.translate("次の料理名を日本語に翻訳してください: " + choices[i]);
            }
            return new Quiz(quizText, choicesJa, correctIdx);
        } catch (Exception e) {
            // API通信やJSONパース失敗時
            return new Quiz("エラーが発生しました: " + e.getMessage(), new String[] { "-", "-", "-", "-" }, 0);
        }
    }

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        Quiz quiz = getQuiz();
        System.out.println(quiz.question);
        if (quiz.choices[0].equals("-") && quiz.correctIdx == 0) {
            // エラー時
            System.out.println("APIまたはデータ取得エラーです。終了します。");
            scanner.close();
            return;
        }
        System.out.print("答えを入力してください");
        String answer = scanner.nextLine().trim();
        boolean isCorrect = false;
        // 番号で判定
        try {
            int idx = Integer.parseInt(answer) - 1;
            if (idx == quiz.correctIdx) {
                isCorrect = true;
            }
        } catch (NumberFormatException e) {
            // 料理名で判定（日本語選択肢と比較）
            if (quiz.correctIdx >= 0 && answer.equalsIgnoreCase(quiz.choices[quiz.correctIdx])) {
                isCorrect = true;
            }
        }
        if (isCorrect) {
            System.out.println("正解");
        } else {
            System.out.println("不正解");
        }
        scanner.close();
    }
}
