import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import org.json.*;

public class Celestial {
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
        // 宇宙豆知識API（太陽系天体API）
        String endpoint = "https://api.le-systeme-solaire.net/rest/bodies/";
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject obj = new JSONObject(response.body());
            JSONArray arr = obj.getJSONArray("bodies");
            // 天体名と説明をリスト化
            List<Body> bodies = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject b = arr.getJSONObject(i);
                String name = b.optString("englishName", "");
                String desc = b.optString("discoveredBy", "");
                if (name.isEmpty())
                    continue;
                // 説明文は発見者や備考などを利用
                String info = "";
                if (!desc.isEmpty())
                    info = "発見者: " + desc;
                String note = b.optString("discoveryDate", "");
                if (!note.isEmpty())
                    info += (info.isEmpty() ? "" : ", ") + "発見日: " + note;
                if (info.isEmpty())
                    info = "太陽系の天体です。";
                bodies.add(new Body(name, info));
            }
            if (bodies.size() < 4)
                throw new Exception("天体データが不足");
            Collections.shuffle(bodies);
            Body answer = bodies.get(0);
            // 正解天体名を日本語に翻訳
            String answerJa = GeminiClient.translate("天体名を日本語に翻訳してください: " + answer.name + "。解説文や記号は不要です。\n")
                    .replaceAll("[\r\n]+", "").trim();
            // 問題文を自然な日本語の文章として生成
            String promptQ = "以下の天体の特徴や発見情報をもとに、天体名を当てる日本語のクイズ問題文を1文で作成してください。問題文は「この天体は…ですが、何でしょう？」のような形式で、説明文をそのまま使わず自然な文章にしてください。解説や選択肢は不要です。\n特徴: "
                    + answer.info;
            String question = GeminiClient.translate(promptQ).replaceAll("[\r\n]+", " ").trim();
            // 誤答生成（AIで似た天体名を日本語で3つ生成）
            String prompt = "「" + answerJa + "」と間違えやすい天体名を3つ日本語で挙げてください。解説文とアスタリスクは不要です。";
            String aiChoices = GeminiClient.translate(prompt);
            List<String> choicesList = new ArrayList<>();
            for (String line : aiChoices.split("\n")) {
                String c = line.replaceAll("^[-・●*\\d.\s]*", "").replaceAll("[*]", "").trim();
                if (!c.isEmpty() && !c.equals(answerJa) && !choicesList.contains(c)) {
                    choicesList.add(c);
                }
            }
            // 3つ未満ならダミーで埋める
            while (choicesList.size() < 3) {
                String dummy = "天体名" + (choicesList.size() + 1);
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
            return new Quiz("宇宙データが取得できませんでした。", new String[] { "-", "-", "-", "-" }, 0);
        }
    }

    static class Body {
        String name, info;

        Body(String name, String info) {
            this.name = name;
            this.info = info;
        }
    }
}
