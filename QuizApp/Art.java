package QuizApp;

public class Art {
    // CLI用：問題文・選択肢・正解インデックスを返す
    public static Quiz getQuiz() {
        try {
            String[] keywords = {
                // 人名（25）
                "レオナルド・ダ・ヴィンチ", "ミケランジェロ", "ラファエロ", "フェルメール", "レンブラント",
                "ゴッホ", "モネ", "マネ", "セザンヌ", "ルノワール",
                "ピカソ", "マティス", "ダリ", "クレー", "ムンク",
                "葛飾北斎", "伊藤若冲", "狩野永徳", "岸田劉生", "岡本太郎",
                "草間彌生", "奈良美智", "千住博", "村上隆", "横尾忠則",
                "モナ・リザ", "最後の晩餐", "アテナイの学堂", "真珠の耳飾りの少女", "夜警",
                "ひまわり", "星月夜", "睡蓮", "草上の昼食", "オランピア",
                "大浴女", "ゲルニカ", "アヴィニョンの娘たち", "ダンス", "記憶の固執",
                "叫び", "富嶽三十六景", "風神雷神図屏風", "洛中洛外図", "麗子像",
                "太陽の塔", "南瓜", "森の子供", "滝", "五百羅漢図"
            };
            java.util.List<String> wordList = new java.util.ArrayList<>(java.util.Arrays.asList(keywords));
            java.util.Collections.shuffle(wordList);
            String answer = wordList.get(0);
            // jisho.org APIで意味を取得
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
            String meaning = "意味が取得できませんでした。";
            org.json.JSONArray dataArr = json.optJSONArray("data");
            if (dataArr != null && dataArr.length() > 0) {
                org.json.JSONObject first = dataArr.getJSONObject(0);
                org.json.JSONArray senses = first.optJSONArray("senses");
                if (senses != null && senses.length() > 0) {
                    org.json.JSONObject sense = senses.getJSONObject(0);
                    org.json.JSONArray jpDefs = sense.optJSONArray("japanese_definitions");
                    if (jpDefs != null && jpDefs.length() > 0) {
                        meaning = jpDefs.join("、").replaceAll("\"", "");
                    } else {
                        org.json.JSONArray defs = sense.optJSONArray("english_definitions");
                        if (defs != null && defs.length() > 0) {
                            meaning = defs.join(", ").replaceAll("\"", "");
                        }
                    }
                }
            }
            // 選択肢4つをランダムに選ぶ（重複なし）
            java.util.LinkedHashSet<String> choicesSet = new java.util.LinkedHashSet<>();
            choicesSet.add(answer);
            for (String w : wordList) {
                if (!w.equals(answer) && choicesSet.size() < 4)
                    choicesSet.add(w);
            }
            java.util.List<String> choicesList = new java.util.ArrayList<>(choicesSet);
            java.util.Collections.shuffle(choicesList);
            String[] choices = choicesList.toArray(new String[0]);
            int correctIdx = 0;
            for (int i = 0; i < choices.length; i++) {
                if (choices[i].equals(answer))
                    correctIdx = i;
            }
            // Gemini APIに意味・用例・選択肢・正解を渡して自然な問題文を生成
            String apikey = System.getenv("GEMINI_API_KEY");
            if (apikey == null)
                throw new Exception("APIキー未設定");
            StringBuilder detail = new StringBuilder();
            detail.append("意味: ").append(meaning);
            if (dataArr != null && dataArr.length() > 0) {
                org.json.JSONObject first = dataArr.getJSONObject(0);
                org.json.JSONArray senses = first.optJSONArray("senses");
                if (senses != null && senses.length() > 0) {
                    org.json.JSONObject sense = senses.getJSONObject(0);
                    org.json.JSONArray exs = sense.optJSONArray("examples");
                    if (exs != null && exs.length() > 0) {
                        detail.append("。用例: ");
                        for (int i = 0; i < exs.length(); i++) {
                            detail.append(exs.getString(i));
                            if (i < exs.length() - 1)
                                detail.append(" / ");
                        }
                    }
                }
            }
            String prompt = "芸術クイズの問題文を作成してください。説明:『" + detail.toString() + "』。正解は『" + answer
                    + "』です。問題文のみ日本語で自然に出力してください。";
            String question = QuizApp.GeminiClient.queryGemini(prompt, apikey);
            if (question == null || question.isEmpty())
                question = "問題文の取得に失敗しました。";
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
