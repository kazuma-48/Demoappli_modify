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
            // 選択肢（正解＋AI生成3つ）
            List<String> choicesList = new ArrayList<>();
            choicesList.add(titleJa);
            for (String dish : dishList) {
                if (choicesList.size() < 4)
                    choicesList.add(dish);
            }
            // 4つ未満ならダミーで埋める
            while (choicesList.size() < 4)
                choicesList.add("料理名不明");
            Collections.shuffle(choicesList);
            String[] choices = choicesList.toArray(new String[0]);
            int correctIdxJa = -1;
            for (int i = 0; i < choices.length; i++) {
                if (choices[i].equals(titleJa))
                    correctIdxJa = i;
            }
            // Geminiでクイズ形式の日本語に変換
            StringBuilder quizPrompt = new StringBuilder();
            quizPrompt.append(String.join(", ", ingredientNames) + "\n");
            quizPrompt.append("選択肢:\n");
            for (int i = 0; i < choices.length; i++) {
                quizPrompt.append((i + 1) + ". " + choices[i] + "\n");
            }
            String quizText = GeminiClient.translate("次の文章を日本語のクイズ形式で自然に表示してください: " + quizPrompt.toString() + "解説文は出力しないでください。");
            // 選択肢はすでに日本語化済み、正解インデックスも計算済み
            return new Quiz(quizText, choices, correctIdxJa);
        } catch (Exception e) {
            // API通信やJSONパース失敗時
            return new Quiz("エラーが発生しました: " + e.getMessage(), new String[] { "-", "-", "-", "-" }, 0);
        }
    }

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            Quiz quiz = getQuiz();
            // クイズ形式で問題文と選択肢のみ表示
            System.out.println(quiz.question);
            if (quiz.choices[0].equals("-") && quiz.correctIdx == 0) {
                // エラー時
                System.out.println("APIまたはデータ取得エラーです。終了します。");
                break;
            }
            System.out.print("答えを入力してください（番号または料理名/qで終了）: ");
            String answer = scanner.nextLine().trim();
            if (answer.equalsIgnoreCase("q")) {
                System.out.println("終了します。");
                break;
            }
            boolean isCorrect = false;
            int answerIdx = -1;
            // 番号で判定
            try {
                answerIdx = Integer.parseInt(answer) - 1;
                if (answerIdx == quiz.correctIdx) {
                    isCorrect = true;
                }
            } catch (NumberFormatException e) {
                // 料理名で判定（日本語選択肢と比較）
                if (quiz.correctIdx >= 0 && answer.equalsIgnoreCase(quiz.choices[quiz.correctIdx])) {
                    isCorrect = true;
                    answerIdx = quiz.correctIdx;
                }
            }
            if (isCorrect) {
                System.out.println("正解！");
            } else {
                System.out.println("不正解。");
            }
            if (answerIdx >= 0 && answerIdx < quiz.choices.length) {
                System.out.println("あなたの答え: " + quiz.choices[answerIdx]);
            }
            System.out.println("正解: " + quiz.choices[quiz.correctIdx]);
        }
        scanner.close();
    }
}
