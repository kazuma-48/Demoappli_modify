package QuizApp;

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
            java.util.List<String> wordList = java.util.Arrays.asList(keywords);
            java.util.Collections.shuffle(wordList);
            String answer = wordList.get(0);
            // jisho.org APIで読み（reading）を取得
            String apiUrl = "https://jisho.org/api/v1/search/words?keyword="
                    + java.net.URLEncoder.encode(answer, "UTF-8");
            java.net.URL url = new java.net.URL(apiUrl);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            java.io.BufferedReader in = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            conn.disconnect();
            org.json.JSONObject json = new org.json.JSONObject(content.toString());
            String reading = "読みが取得できませんでした。";
            org.json.JSONArray dataArr = json.optJSONArray("data");
            if (dataArr != null && dataArr.length() > 0) {
                org.json.JSONObject first = dataArr.getJSONObject(0);
                org.json.JSONArray japaneseArr = first.optJSONArray("japanese");
                if (japaneseArr != null && japaneseArr.length() > 0) {
                    org.json.JSONObject japanese = japaneseArr.getJSONObject(0);
                    reading = japanese.optString("reading", reading);
                }
            }
            // 読みが取得できなかった場合はGeminiに問い合わせ
            if (reading.equals("読みが取得できませんでした。")) {
                String apikey = System.getenv("GEMINI_API_KEY");
                if (apikey == null)
                    throw new Exception("APIキー未設定");
                String prompt = "漢字『" + answer + "』の正しい読み（ひらがな）を1つだけ出力してください。";
                String aiReading = QuizApp.GeminiClient.queryGemini(prompt, apikey);
                if (aiReading != null && !aiReading.trim().isEmpty()) {
                    reading = aiReading.trim().split("\n")[0].replaceAll("^[0-9]+[.\\-\\s]*", "").trim();
                }
            }
            // Gemini APIで間違いの読みを3つ生成
            String apikey = System.getenv("GEMINI_API_KEY");
            if (apikey == null)
                throw new Exception("APIキー未設定");
            String prompt = "漢字『" + answer + "』の正しい読みは『" + reading + "』です。間違いの選択肢として自然な日本語の読みを3つ生成してください。\n" +
                    "1. 実在しそうな読みであること\n2. 正解と紛らわしいこと\n3. ひらがなで出力し、改行区切りで3つのみ出力してください。";
            String aiChoicesResponse = QuizApp.GeminiClient.queryGemini(prompt, apikey);
            String[] aiChoices = aiChoicesResponse.trim().split("\n");
            // クリーンアップ
            for (int i = 0; i < aiChoices.length; i++) {
                aiChoices[i] = aiChoices[i].replaceAll("^[0-9]+[.\\-\\s]*", "").trim();
            }
            // 選択肢配列を作成（正解 + AI生成の3つ）
            java.util.LinkedHashSet<String> choicesSet = new java.util.LinkedHashSet<>();
            choicesSet.add(reading);
            for (int i = 0; i < Math.min(3, aiChoices.length); i++) {
                if (!aiChoices[i].isEmpty() && !aiChoices[i].equals(reading)) {
                    choicesSet.add(aiChoices[i]);
                }
            }
            // 足りない場合はダミーで補完
            while (choicesSet.size() < 4) {
                choicesSet.add("だみー");
            }
            java.util.List<String> choicesList = new java.util.ArrayList<>(choicesSet);
            java.util.Collections.shuffle(choicesList);
            String[] choices = choicesList.toArray(new String[0]);
            int correctIdx = 0;
            for (int i = 0; i < choices.length; i++) {
                if (choices[i].equals(reading))
                    correctIdx = i;
            }
            String question = answer;
            return new Quiz(question, choices, correctIdx);
        } catch (Exception e) {
            String[] choices = { "エラー", "", "", "" };
            return new Quiz("APIからクイズを取得できませんでした。", choices, 0);
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
