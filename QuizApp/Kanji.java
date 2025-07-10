package QuizApp;

import org.json.JSONObject;
import org.json.JSONArray;

public class Kanji {
    // CLI用：問題文・選択肢・正解インデックスを返す
    public static Quiz getQuiz() {
        try {
            String[] keywords = {
                    "曖昧", "宛先", "嵐光", "畏怖", "萎縮", "椅子", "語彙力", "茨城県", "嗚咽", "怨望", "淑媛", "妖艶", "臆見", "苛禁",
                    "楷則", "諧謔", "蓋世", "樽柿", "玩具", "亀甲", "破毀", "石臼", "僅僅", "窟穴", "熊襲", "造詣", "空隙", "挙兵",
                    "舷窓", "股肱", "猛虎", "桔梗", "倨傲", "頃合い", "沙羅双樹", "頓挫", "塞源", "刹那", "食餌", "馬鹿", "叱咤",
                    "腫物", "羞恥心", "丼", "比喩", "脇脳", "美貌", "侮蔑", "賭博", "臥薪嘗胆"
            };
            java.util.List<String> wordList = new java.util.ArrayList<>(java.util.Arrays.asList(keywords));
            java.util.Collections.shuffle(wordList);
            String answer = wordList.get(0);

            // jisho.org APIで漢字の読みを取得
            String apiUrl = "https://jisho.org/api/v1/search/words?keyword="
                    + java.net.URLEncoder.encode(answer, "UTF-8");
            java.net.URI uri = java.net.URI.create(apiUrl);
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();
            java.net.http.HttpResponse<String> response = client.send(request,
                    java.net.http.HttpResponse.BodyHandlers.ofString());

            JSONObject json = new JSONObject(response.body());
            String correctReading = "よみがな取得できませんでした";
            JSONArray dataArr = json.optJSONArray("data");
            if (dataArr != null && dataArr.length() > 0) {
                JSONObject first = dataArr.getJSONObject(0);
                JSONArray japanese = first.optJSONArray("japanese");
                if (japanese != null && japanese.length() > 0) {
                    JSONObject firstJapanese = japanese.getJSONObject(0);
                    String reading = firstJapanese.optString("reading");
                    if (reading != null && !reading.isEmpty()) {
                        correctReading = reading;
                    }
                }
            }

            // Gemini APIキーの確認
            String apikey = System.getenv("GEMINI_API_KEY");
            if (apikey == null)
                throw new Exception("APIキー未設定");

            // AIで似た読みの間違った選択肢を3つ生成
            String choicesPrompt = "漢字「" + answer + "」の読み「" + correctReading + "」に似た読み方を3つ生成してください。" +
                    "以下の条件を満たしてください：\n" +
                    "1. ひらがなで出力すること\n" +
                    "2. 実際にありそうな読み方であること\n" +
                    "3. 正解と音が似ていて混同しやすいこと\n" +
                    "4. 読み方のみを出力し、説明は不要\n" +
                    "5. 改行区切りで3つの読み方のみを出力してください";

            String aiChoicesResponse = QuizApp.GeminiClient.queryGemini(choicesPrompt, apikey);
            String[] aiChoices = aiChoicesResponse.trim().split("\n");

            // AI生成の選択肢をクリーンアップ
            for (int i = 0; i < aiChoices.length; i++) {
                aiChoices[i] = aiChoices[i].replaceAll("^[0-9]+[.\\-\\s]*", "").trim();
                aiChoices[i] = aiChoices[i].replaceAll("[「」]", "").trim();
            }

            // 選択肢配列を作成（正解 + AI生成の3つ）
            java.util.List<String> choicesList = new java.util.ArrayList<>();
            choicesList.add(correctReading);

            // AI生成の選択肢を追加（最大3つ）
            for (int i = 0; i < Math.min(3, aiChoices.length); i++) {
                if (!aiChoices[i].isEmpty() && !aiChoices[i].equals(correctReading)) {
                    choicesList.add(aiChoices[i]);
                }
            }

            // 足りない場合はデフォルトの読み方で補完
            String[] defaultReadings = { "あいまい", "ばんせん", "らんこう", "いふ", "いしゅく" };
            if (choicesList.size() < 4) {
                for (String reading : defaultReadings) {
                    if (!choicesList.contains(reading) && choicesList.size() < 4) {
                        choicesList.add(reading);
                    }
                }
            }

            // 選択肢をシャッフル
            java.util.Collections.shuffle(choicesList);
            // 選択肢をシャッフル
            java.util.Collections.shuffle(choicesList);
            String[] choices = choicesList.toArray(new String[0]);

            int correctIdx = 0;
            for (int i = 0; i < choices.length; i++) {
                if (choices[i].equals(correctReading))
                    correctIdx = i;
            }

            // 問題文を漢字クイズ用に設定
            String question = "次の漢字の読み方として正しいものはどれですか？\n\n【" + answer + "】";

            return new Quiz(question, choices, correctIdx);
        } catch (Exception e) {
            String[] choices = { "エラー", "", "", "" };
            return new Quiz("APIから漢字クイズを取得できませんでした。エラー: " + e.getMessage(), choices, 0);
        }
    }

    // クイズデータ用クラス
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
