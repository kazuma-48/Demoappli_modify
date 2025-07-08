package QuezApp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;
import org.json.JSONArray;

public class English {
    // Oxford Dictionaries APIを使って英単語クイズを生成
    public static String getQuizHtml() {
        String[] words = { "apple", "river", "mountain", "freedom", "history", "science", "friend", "music", "peace",
                "future" };
        String[] japanese = { "りんご", "川", "山", "自由", "歴史", "科学", "友達", "音楽", "平和", "未来" };
        String question = "";
        String answer = "";
        String[] choices = new String[4];
        try {
            // ランダムに1単語選択
            int idx = (int) (Math.random() * words.length);
            answer = words[idx];
            String answerJa = japanese[idx];
            // Oxford Dictionaries APIで意味を取得（APIキー・IDは環境変数や定数で設定してください）
            String appId = System.getenv("OXFORD_APP_ID");
            String appKey = System.getenv("OXFORD_APP_KEY");
            String apiUrl = "https://od-api.oxforddictionaries.com/api/v2/entries/en-us/" + answer;
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("app_id", appId);
            conn.setRequestProperty("app_key", appKey);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            conn.disconnect();
            JSONObject json = new JSONObject(content.toString());
            String meaning = "意味が取得できませんでした。";
            JSONArray results = json.optJSONArray("results");
            if (results != null && results.length() > 0) {
                JSONArray lexicalEntries = results.getJSONObject(0).optJSONArray("lexicalEntries");
                if (lexicalEntries != null && lexicalEntries.length() > 0) {
                    JSONArray entries = lexicalEntries.getJSONObject(0).optJSONArray("entries");
                    if (entries != null && entries.length() > 0) {
                        JSONArray senses = entries.getJSONObject(0).optJSONArray("senses");
                        if (senses != null && senses.length() > 0) {
                            JSONArray defs = senses.getJSONObject(0).optJSONArray("definitions");
                            if (defs != null && defs.length() > 0) {
                                meaning = defs.getString(0);
                            }
                        }
                    }
                }
            }
            question = "次の意味に当てはまる英単語の日本語訳はどれ？<br>" + meaning;
            // 選択肢（正解＋ダミー3つ）
            java.util.List<Integer> idxList = new java.util.ArrayList<>();
            for (int i = 0; i < words.length; i++)
                idxList.add(i);
            java.util.Collections.shuffle(idxList);
            java.util.List<String> choiceList = new java.util.ArrayList<>();
            choiceList.add(answerJa);
            int count = 0;
            for (int i : idxList) {
                if (i != idx && count < 3) {
                    choiceList.add(japanese[i]);
                    count++;
                }
            }
            java.util.Collections.shuffle(choiceList);
            for (int i = 0; i < 4; i++)
                choices[i] = choiceList.get(i);
        } catch (Exception e) {
            question = "APIからクイズを取得できませんでした。";
            answer = "エラー";
            choices = new String[] { "エラー", "", "", "" };
            e.printStackTrace();
        }
        int correctIdx = 0;
        for (int i = 0; i < 4; i++) {
            if (choices[i].equals(japanese[(java.util.Arrays.asList(words)).indexOf(answer)]))
                correctIdx = i;
        }
        StringBuilder html = new StringBuilder();
        html.append("<div id='englishQuiz'>");
        html.append("<h2>英語クイズ</h2>");
        html.append("<div class='question-box'>問題：" + question + "</div>");
        for (int i = 0; i < 4; i++) {
            html.append("<button onclick=\"checkEnglishAnswer(" + correctIdx + "," + i
                    + ")\" style='font-size:30px; margin:10px;' class='choice-btn'>" + choices[i] + "</button><br>");
        }
        html.append("<div id='englishResult'></div>");
        html.append(
                "<button class='home-btn' onclick='showNextEnglishQuiz();' style='margin-right:20px;'>新しい問題を取得</button>");
        html.append("<button class='home-btn' onclick='showHomeFromQuiz()'>ホーム画面に戻る</button>");
        html.append("</div>");
        html.append("<script>\n" +
                "function checkEnglishAnswer(correct, a) {\n" +
                "  if(a == correct) {\n" +
                "    document.getElementById('englishResult').innerHTML = '正解！';\n" +
                "  } else {\n" +
                "    document.getElementById('englishResult').innerHTML = '不正解';\n" +
                "  }\n" +
                "}\n" +
                "function showNextEnglishQuiz() {\n" +
                "  location.href = location.href;\n" +
                "}\n" +
                "</script>");
        return html.toString();
    }

    // CLI用：問題文・選択肢・正解インデックスを返す
    public static Quiz getQuiz() {
        String[] words = { "apple", "river", "mountain", "freedom", "history", "science", "friend", "music", "peace",
                "future" };
        String[] japanese = { "りんご", "川", "山", "自由", "歴史", "科学", "友達", "音楽", "平和", "未来" };
        String question = "";
        String answer = "";
        String[] choices = new String[4];
        int correctIdx = 0;
        try {
            int idx = (int) (Math.random() * words.length);
            answer = words[idx];
            String answerJa = japanese[idx];
            String appId = System.getenv("OXFORD_APP_ID");
            String appKey = System.getenv("OXFORD_APP_KEY");
            String apiUrl = "https://od-api.oxforddictionaries.com/api/v2/entries/en-us/" + answer;
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("app_id", appId);
            conn.setRequestProperty("app_key", appKey);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            conn.disconnect();
            JSONObject json = new JSONObject(content.toString());
            String meaning = "意味が取得できませんでした。";
            JSONArray results = json.optJSONArray("results");
            if (results != null && results.length() > 0) {
                JSONArray lexicalEntries = results.getJSONObject(0).optJSONArray("lexicalEntries");
                if (lexicalEntries != null && lexicalEntries.length() > 0) {
                    JSONArray entries = lexicalEntries.getJSONObject(0).optJSONArray("entries");
                    if (entries != null && entries.length() > 0) {
                        JSONArray senses = entries.getJSONObject(0).optJSONArray("senses");
                        if (senses != null && senses.length() > 0) {
                            JSONArray defs = senses.getJSONObject(0).optJSONArray("definitions");
                            if (defs != null && defs.length() > 0) {
                                meaning = defs.getString(0);
                            }
                        }
                    }
                }
            }
            question = "次の意味に当てはまる英単語の日本語訳はどれ？\n" + meaning;
            java.util.List<Integer> idxList = new java.util.ArrayList<>();
            for (int i = 0; i < words.length; i++)
                idxList.add(i);
            java.util.Collections.shuffle(idxList);
            java.util.List<String> choiceList = new java.util.ArrayList<>();
            choiceList.add(answerJa);
            int count = 0;
            for (int i : idxList) {
                if (i != idx && count < 3) {
                    choiceList.add(japanese[i]);
                    count++;
                }
            }
            java.util.Collections.shuffle(choiceList);
            for (int i = 0; i < 4; i++)
                choices[i] = choiceList.get(i);
            for (int i = 0; i < 4; i++) {
                if (choices[i].equals(answerJa))
                    correctIdx = i;
            }
        } catch (Exception e) {
            question = "APIからクイズを取得できませんでした。";
            choices = new String[] { "エラー", "", "", "" };
            correctIdx = 0;
        }
        return new Quiz(question, choices, correctIdx);
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
