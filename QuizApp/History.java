package QuizApp;

import org.json.JSONObject;
import org.json.JSONArray;

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
            java.net.URI uri = java.net.URI.create(apiUrl);
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();
            java.net.http.HttpResponse<String> response = client.send(request,
                    java.net.http.HttpResponse.BodyHandlers.ofString());

            JSONObject json = new JSONObject(response.body());
            String meaning = "意味が取得できませんでした。";
            JSONArray dataArr = json.optJSONArray("data");
            if (dataArr != null && dataArr.length() > 0) {
                JSONObject first = dataArr.getJSONObject(0);
                JSONArray senses = first.optJSONArray("senses");
                if (senses != null && senses.length() > 0) {
                    JSONObject sense = senses.getJSONObject(0);
                    JSONArray jpDefs = sense.optJSONArray("japanese_definitions");
                    if (jpDefs != null && jpDefs.length() > 0) {
                        meaning = jpDefs.join("、").replaceAll("\"", "");
                    } else {
                        JSONArray defs = sense.optJSONArray("english_definitions");
                        if (defs != null && defs.length() > 0) {
                            meaning = defs.join(", ").replaceAll("\"", "");
                        }
                    }
                }
            }

            // Gemini APIキーの確認
            String apikey = System.getenv("GEMINI_API_KEY");
            if (apikey == null)
                throw new Exception("APIキー未設定");

            // AIで正解に似た間違った選択肢を3つ生成
            String choicesPrompt = "日本史用語「" + answer + "」に似た日本史用語を3つ生成してください。" +
                    "以下の条件を満たしてください：\n" +
                    "1. 実在する日本史用語であること\n" +
                    "2. 正解と混同しやすい用語であること\n" +
                    "3. 同じ時代や関連する分野の用語であること\n" +
                    "4. 単語のみを出力し、説明は不要\n" +
                    "5. 改行区切りで3つの単語のみを出力してください";

            String aiChoicesResponse = QuizApp.GeminiClient.queryGemini(choicesPrompt, apikey);
            String[] aiChoices = aiChoicesResponse.trim().split("\n");

            // AI生成の選択肢をクリーンアップ（番号や余計な文字を除去）
            for (int i = 0; i < aiChoices.length; i++) {
                aiChoices[i] = aiChoices[i].replaceAll("^[0-9]+[.\\-\\s]*", "").trim();
                aiChoices[i] = aiChoices[i].replaceAll("[「」]", "").trim();
            }

            // 選択肢配列を作成（正解 + AI生成の3つ）
            java.util.List<String> choicesList = new java.util.ArrayList<>();
            choicesList.add(answer);

            // AI生成の選択肢を追加（最大3つ）
            for (int i = 0; i < Math.min(3, aiChoices.length); i++) {
                if (!aiChoices[i].isEmpty() && !aiChoices[i].equals(answer)) {
                    choicesList.add(aiChoices[i]);
                }
            }

            // 足りない場合はキーワードリストから補完
            if (choicesList.size() < 4) {
                for (String w : wordList) {
                    if (!choicesList.contains(w) && choicesList.size() < 4) {
                        choicesList.add(w);
                    }
                }
            }

            // 選択肢をシャッフル
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
                JSONObject first = dataArr.getJSONObject(0);
                JSONArray senses = first.optJSONArray("senses");
                if (senses != null && senses.length() > 0) {
                    JSONObject sense = senses.getJSONObject(0);
                    JSONArray exs = sense.optJSONArray("examples");
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
            String prompt = "日本史用語クイズの問題文を作成してください。説明:『" + detail.toString() + "』。正解は『" + answer
                    + "』です。問題文のみ日本語で自然に出力してください。";
            String question = QuizApp.GeminiClient.queryGemini(prompt, apikey);
            if (question == null || question.isEmpty())
                question = "問題文の取得に失敗しました。";
            return new Quiz(question, choices, correctIdx);
        } catch (Exception e) {
            String[] choices = { "エラー", "", "", "" };
            return new Quiz("APIからクイズを取得できませんでした。エラー: " + e.getMessage(), choices, 0);
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
