import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Cat {
    // Cat Facts APIからランダムな猫の豆知識を取得し、正誤クイズを生成
    public static Quiz getQuiz() {
        String apiUrl = "https://catfact.ninja/fact";
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String json = response.body();
            // 簡易的なJSONパース（本格運用はライブラリ推奨）
            String fact = "";
            int idx = json.indexOf("\"fact\":");
            if (idx != -1) {
                int start = json.indexOf('"', idx + 7) + 1;
                int end = json.indexOf('"', start);
                if (start > 0 && end > start) {
                    fact = json.substring(start, end);
                }
            }
            if (fact.isEmpty()) {
                return new Quiz("猫の豆知識が取得できませんでした。", new String[] { "-", "-" }, 0);
            }
            // GeminiAPIで日本語に翻訳（正しい文）
            String jpFact = GeminiClient.translate("次の英文を自然な日本語に翻訳してください。\n" + fact);
            // GeminiAPIで誤りの文を生成
            String wrongPrompt = "次の猫の豆知識（英文）をもとに、内容を一部事実と異なるように改変した日本語の文を1つ作ってください。*は出力しないでください。自然な日本語で、内容が本当ではないようにしてください。";
            String jpWrongFact = GeminiClient.translate(wrongPrompt + "\n" + fact);

            // 正しい問題文と誤り問題文をランダムで出題
            boolean isCorrect = Math.random() < 0.5;
            String question;
            int correctIdx;
            if (isCorrect) {
                question = "次の猫の豆知識は本当でしょうか？\n" + jpFact;
                correctIdx = 0;
            } else {
                question = "次の猫の豆知識は本当でしょうか？\n" + jpWrongFact;
                correctIdx = 1;
            }
            String[] choices = { "正しい", "誤り" };
            return new Quiz(question, choices, correctIdx);
        } catch (Exception e) {
            return new Quiz("猫の豆知識が取得できませんでした。", new String[] { "-", "-" }, 0);
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
}
