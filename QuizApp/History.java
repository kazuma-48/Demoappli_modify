package QuizApp;

public class History {
    // CLI用：問題文・選択肢・正解インデックスを返す
    public static Quiz getQuiz() {
        try {
            String[] keywords = {
                    "戦国", "幕府", "天皇", "将軍", "条約", "大名", "朝廷", "維新", "元号", "藩主",
                    "公家", "摂関", "征夷大将軍", "幕末", "明治時代", "江戸時代", "鎌倉時代", "室町時代", "安土桃山時代", "奈良時代",
                    "平安時代", "飛鳥時代", "古墳時代", "弥生時代", "縄文時代", "士族", "豪族", "藩士", "摂政", "関白",
                    "幕臣", "外様大名", "譜代大名", "御家人", "奉行", "藩政改革", "藩政維新", "藩政時代", "聖徳太子", "平清盛",
                    "聖武天皇", "北条政子", "織田信長", "豊臣秀吉", "徳川家康", "明智光秀", "フランシスコ・シャビエル", "西郷隆盛", "大久保利道", "岩倉具視",
                    "板垣退助", "伊藤博文", "木戸孝允"
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
            // Gemini APIで正解と似ている歴史用語を3つ生成
            String apikey = System.getenv("GEMINI_API_KEY");
            if (apikey == null)
                throw new Exception("APIキー未設定");
            String similarPrompt = "歴史用語『" + answer + "』に似ている、紛らわしい日本の歴史用語を3つ生成してください。\n" +
                    "1. 実在する歴史用語であること\n2. 正解と混同しやすいこと\n3. 1行に1つ、改行区切りで3つのみ出力してください。";
            String aiChoicesResponse = QuizApp.GeminiClient.queryGemini(similarPrompt, apikey);
            String[] aiChoices = aiChoicesResponse.trim().split("\n");
            // クリーンアップ
            for (int i = 0; i < aiChoices.length; i++) {
                aiChoices[i] = aiChoices[i].replaceAll("^[0-9]+[.\\-\\s]*", "").trim();
            }
            // 選択肢配列を作成（正解 + AI生成の3つ）
            java.util.LinkedHashSet<String> choicesSet = new java.util.LinkedHashSet<>();
            choicesSet.add(answer);
            for (int i = 0; i < Math.min(3, aiChoices.length); i++) {
                if (!aiChoices[i].isEmpty() && !aiChoices[i].equals(answer)) {
                    choicesSet.add(aiChoices[i]);
                }
            }
            // 足りない場合はランダムで補完
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
            String prompt = "歴史史用語クイズの問題文を作成してください。説明:『" + detail.toString() + "』。正解は『" + answer
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
