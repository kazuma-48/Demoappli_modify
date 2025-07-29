import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import org.json.*;

public class Dog {
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
        // The Dog APIで犬種リスト取得
        String endpoint = "https://api.thedogapi.com/v1/breeds";
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONArray arr = new JSONArray(response.body());
            List<String> breeds = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String breed = obj.optString("name", "");
                if (!breed.isEmpty() && !breeds.contains(breed)) {
                    breeds.add(breed);
                }
            }
            if (breeds.size() < 4)
                throw new Exception("犬種データが不足");
            Collections.shuffle(breeds);
            String answer = breeds.get(0);
            // 問題文生成（Geminiで犬種の特徴を日本語で説明）
            String promptQ = "犬種『" + answer + "』の特徴や性格を日本語で簡潔に説明し、クイズ問題文として1文で作成してください。犬種名は問題文に含めず、解説や選択肢は不要です。";
            String question = GeminiClient.translate(promptQ).replaceAll("[\r\n]+", " ").trim();
            // 正解犬種名を日本語に翻訳
            String answerJa = GeminiClient.translate("犬種名を日本語に翻訳してください: " + answer + "。解説文や記号は不要です。\n")
                    .replaceAll("[\r\n]+", "").trim();
            // 誤答生成（AIで似た犬種名を日本語で3つ生成）
            String prompt = "「" + answerJa + "」と間違えやすい犬種名を3つ日本語で挙げてください。解説文とアスタリスクは不要です。";
            String aiChoices = GeminiClient.translate(prompt);
            List<String> choicesList = new ArrayList<>();
            for (String line : aiChoices.split("\n")) {
                // 記号・数字・箇条書き記号・空白を除去し、純粋な犬種名のみ
                String c = line.replaceAll("^[\\p{Punct}・●\\d.\\s]*", "").replaceAll("[\\p{Punct}・●\\d.\\s]+$", "")
                        .trim();
                if (!c.isEmpty() && !c.equals(answerJa) && !choicesList.contains(c)) {
                    choicesList.add(c);
                }
            }
            // 3つ未満ならダミーで埋める
            while (choicesList.size() < 3) {
                String dummy = "犬種名" + (choicesList.size() + 1);
                if (!choicesList.contains(dummy))
                    choicesList.add(dummy);
            }
            // 正解をランダムな位置に挿入
            Random rand = new Random();
            int correctIdx = rand.nextInt(4);
            choicesList.add(correctIdx, answerJa);
            String[] choices = choicesList.toArray(new String[0]);
            return new Quiz(question, choices, correctIdx);
        } catch (Exception e) {
            return new Quiz("犬データが取得できませんでした。", new String[] { "-", "-", "-", "-" }, 0);
        }
    }
}
