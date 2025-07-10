package QuizApp;

public class Kanji {
    // CLI用：問題文・選択肢・正解インデックスを返す
    public static Quiz getQuiz() {
        try {
            String[] keywords = {
                    "曖昧","宛先","嵐光","畏怖","萎縮","椅子","語彙力","茨城県","嗚咽","怨望","淑媛","妖艶","臆見","苛禁",
                    "楷則","諧謔","蓋世","樽柿","玩具","亀甲","破毀","石臼","僅僅","窟穴","熊襲","造詣","空隙","挙兵",
                    "舷窓","股肱","猛虎","桔梗","倨傲","頃合い","沙羅双樹","頓挫","塞源","刹那","食餌","馬鹿","叱咤",
                    "腫物","羞恥心","丼","比喩","脇脳","美貌","侮蔑","賭博","臥薪嘗胆"
            };
            java.util.List<String> wordList = java.util.Arrays.asList(keywords);
            java.util.Collections.shuffle(wordList);
            String answer = wordList.get(0);
            String apiUrl = "https://repo1.maven.org/maven2/org/json/json/"
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
            String question = "次の意味に当てはまる日本史用語はどれ？\n" + meaning;
            java.util.List<String> choicesList = new java.util.ArrayList<>();
            choicesList.add(answer);
            for (String w : wordList) {
                if (!w.equals(answer) && choicesList.size() < 4)
                    choicesList.add(w);
            }
            java.util.Collections.shuffle(choicesList);
            String[] choices = choicesList.toArray(new String[0]);
            int correctIdx = 0;
            for (int i = 0; i < 4; i++) {
                if (choices[i].equals(answer))
                    correctIdx = i;
            }
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
