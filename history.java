import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;
import org.json.JSONArray;

public class history {
    // Wikipedia APIからランダムな記事タイトルと説明を取得し、クイズを生成
    public static String getQuizHtml() {
        String question = "";
        String answer = "";
        String[] choices = new String[4];
        try {
            // 4つのランダム記事タイトルを取得
            String apiUrl = "https://ja.wikipedia.org/w/api.php?action=query&list=random&rnnamespace=0&rnlimit=4&format=json";
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            conn.disconnect();
            JSONObject json = new JSONObject(content.toString());
            JSONArray randomArr = json.getJSONObject("query").getJSONArray("random");
            for (int i = 0; i < 4; i++) {
                choices[i] = randomArr.getJSONObject(i).getString("title");
            }
            // 1つ目の記事の説明文を取得
            String titleForDesc = choices[0];
            String descApiUrl = "https://ja.wikipedia.org/w/api.php?action=query&prop=extracts&exintro&explaintext&format=json&titles=" + java.net.URLEncoder.encode(titleForDesc, "UTF-8");
            URL descUrl = new URL(descApiUrl);
            HttpURLConnection descConn = (HttpURLConnection) descUrl.openConnection();
            descConn.setRequestMethod("GET");
            BufferedReader descIn = new BufferedReader(new InputStreamReader(descConn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder descContent = new StringBuilder();
            while ((inputLine = descIn.readLine()) != null) {
                descContent.append(inputLine);
            }
            descIn.close();
            descConn.disconnect();
            JSONObject descJson = new JSONObject(descContent.toString());
            JSONObject pages = descJson.getJSONObject("query").getJSONObject("pages");
            String extract = "";
            for (String key : pages.keySet()) {
                extract = pages.getJSONObject(key).optString("extract", "説明が見つかりませんでした。");
                break;
            }
            question = extract.length() > 0 ? extract : "説明が見つかりませんでした。";
            answer = titleForDesc;
        } catch (Exception e) {
            question = "APIからクイズを取得できませんでした。";
            answer = "エラー";
            choices = new String[]{"エラー", "", "", ""};
        }
        // シャッフル
        java.util.List<String> list = java.util.Arrays.asList(choices);
        java.util.Collections.shuffle(list);
        choices = list.toArray(new String[0]);
        int correctIdx = 0;
        for (int i = 0; i < 4; i++) {
            if (choices[i].equals(answer)) correctIdx = i;
        }
        StringBuilder html = new StringBuilder();
        html.append("<div id='historyQuiz'>");
        html.append("<h2>歴史クイズ</h2>");
        html.append("<p>問題：" + question + "</p>");
        for (int i = 0; i < 4; i++) {
            html.append("<button onclick=\"checkHistoryAnswer(" + correctIdx + "," + i + ")\" style='font-size:30px; margin:10px;'>" + choices[i] + "</button><br>");
        }
        html.append("<div id='historyResult'></div>");
        // ホームに戻るボタンを追加
        html.append("<button class='home-btn' onclick='showHomeFromQuiz()'>ホーム画面に戻る</button>");
        html.append("</div>");
        html.append("<script>\n" +
                "function checkHistoryAnswer(correct, a) {\n" +
                "  if(a == correct) {\n" +
                "    document.getElementById('historyResult').innerHTML = '正解！';\n" +
                "  } else {\n" +
                "    document.getElementById('historyResult').innerHTML = '不正解';\n" +
                "  }\n" +
                "}\n" +
                "</script>");
        return html.toString();
    }
}
