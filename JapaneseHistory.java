import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;
import org.json.JSONArray;

public class JapaneseHistory {
    // Wikipedia APIからランダムな記事タイトルと説明を取得し、クイズを生成
    public static String getQuizHtml() {
        String question = "";
        String answer = "";
        String[] choices = new String[4];
        try {
            // 日本史ワードリスト（拡張版）
            String[] keywords = {
                    "戦国", "幕府", "天皇", "藩", "将軍", "条約", "大名", "朝廷", "維新", "武士", "元号",
                    "藩主", "公家", "摂関", "征夷大将軍", "幕末", "明治時代", "江戸時代", "鎌倉時代", "室町時代", "安土桃山時代", "奈良時代", "平安時代", "飛鳥時代",
                    "古墳時代", "弥生時代", "縄文時代", "士族", "豪族", "藩士", "藩校", "藩政", "藩領", "藩医",
                    // 追加ワード
                    "摂政", "関白", "幕臣", "外様大名", "譜代大名", "親藩", "旗本", "御家人", "直参", "陪臣", "家老", "奉行", "目付", "町奉行", "寺社奉行",
                    "勘定奉行", "老中", "大老", "若年寄", "三奉行", "五奉行", "三職", "五摂家", "公卿", "殿様", "姫君", "侍女", "家臣", "家系", "家紋",
                    "家督", "家伝", "家法", "家訓", "家格", "家柄", "家名", "家宝", "家臣団", "家老職", "家老会議", "藩政改革", "藩札", "藩邸", "藩領",
                    "藩主家", "藩士団", "藩医", "藩学", "藩財政", "藩政期", "藩政時代", "藩政改革", "藩政危機", "藩政崩壊", "藩政末期", "藩政維新",
                    // 有名な日本史用語・人名・藩・家・事件・制度・条約
                    "聖徳太子", "織田信長", "豊臣秀吉", "徳川家康", "足利尊氏", "源頼朝", "平清盛", "卑弥呼", "聖武天皇", "紫式部", "藤原道長", "北条政子", "北条時宗",
                    "明智光秀", "石田三成", "西郷隆盛", "大久保利通", "坂本龍馬", "勝海舟", "伊藤博文", "山県有朋", "板垣退助", "木戸孝允", "近藤勇", "土方歳三",
                    "沖田総司", "新選組", "会津藩", "薩摩藩", "長州藩", "土佐藩", "水戸藩", "加賀藩", "仙台藩", "福岡藩", "熊本藩", "佐賀藩", "庄内藩", "米沢藩",
                    "彦根藩", "岡山藩", "姫路藩", "松江藩", "鳥取藩", "高知藩", "宇和島藩", "松山藩", "徳島藩", "高松藩", "丸亀藩", "今治藩", "大洲藩", "小倉藩",
                    "久留米藩", "柳川藩", "佐土原藩", "鹿児島藩", "島津家", "毛利家", "伊達家", "上杉家", "武田家", "北条家", "細川家", "前田家", "黒田家", "鍋島家",
                    "島原の乱", "応仁の乱", "承久の乱", "元寇", "関ヶ原の戦い", "大坂の陣", "戊辰戦争", "西南戦争", "日清戦争", "日露戦争", "太平洋戦争", "満州事変",
                    "二・二六事件", "五・一五事件", "大政奉還", "廃藩置県", "版籍奉還", "明治維新", "文明開化", "富国強兵", "殖産興業", "学制", "徴兵令", "地租改正",
                    "廃仏毀釈", "自由民権運動", "大日本帝国憲法", "日英同盟", "日独伊三国同盟", "ポツダム宣言", "東京裁判", "日本国憲法", "サンフランシスコ講和条約"
            };
            java.util.List<String> filtered = new java.util.ArrayList<>();
            int tryCount = 0;
            String inputLine;
            while (filtered.size() < 4 && tryCount < 5) { // 最大5回リトライ
                filtered.clear();
                String apiUrl = "https://ja.wikipedia.org/w/api.php?action=query&list=random&rnnamespace=0&rnlimit=200&format=json";
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                conn.disconnect();
                JSONObject json = new JSONObject(content.toString());
                JSONArray randomArr = json.getJSONObject("query").getJSONArray("random");
                for (int i = 0; i < randomArr.length(); i++) {
                    String title = randomArr.getJSONObject(i).getString("title");
                    for (String kw : keywords) {
                        if (title.contains(kw)) {
                            filtered.add(title);
                            break;
                        }
                    }
                }
                tryCount++;
            }
            if (filtered.size() < 4) {
                // 最終手段：ワードリストからダミー問題
                java.util.Collections.shuffle(java.util.Arrays.asList(keywords));
                for (int i = 0; i < 4; i++)
                    filtered.add(keywords[i]);
            }
            // 4件をランダムに選ぶ
            java.util.Collections.shuffle(filtered);
            for (int i = 0; i < 4; i++) {
                choices[i] = filtered.get(i);
            }
            // 1つ目の記事の説明文を取得
            String titleForDesc = choices[0];
            String descApiUrl = "https://ja.wikipedia.org/w/api.php?action=query&prop=extracts&exintro&explaintext&format=json&titles="
                    + java.net.URLEncoder.encode(titleForDesc, "UTF-8");
            URL descUrl = new URL(descApiUrl);
            HttpURLConnection descConn = (HttpURLConnection) descUrl.openConnection();
            descConn.setRequestMethod("GET");
            BufferedReader descIn = new BufferedReader(
                    new InputStreamReader(descConn.getInputStream(), StandardCharsets.UTF_8));
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
            answer = titleForDesc;
            // 問題文から正解用語（タイトル）を除去し、読み仮名（全角カッコ内）も除去
            String sanitizedExtract = extract.replaceAll("（[^）]+）", "");
            sanitizedExtract = sanitizedExtract.replaceAll(java.util.regex.Pattern.quote(answer), "○○");
            question = sanitizedExtract;

            // 不正解選択肢を「正解に似ている日本史ワード」から選ぶ
            java.util.List<String> distractors = new java.util.ArrayList<>();
            for (String kw : keywords) {
                if (!kw.equals(answer)
                        && (answer.contains(kw) || kw.contains(answer) || getLevenshteinDistance(answer, kw) <= 3)) {
                    distractors.add(kw);
                }
            }
            // 3つ未満なら他の日本史ワードから補完
            java.util.Collections.shuffle(distractors);
            java.util.List<String> allKw = new java.util.ArrayList<>();
            for (String kw : keywords)
                if (!kw.equals(answer))
                    allKw.add(kw);
            java.util.Collections.shuffle(allKw);
            while (distractors.size() < 3 && allKw.size() > 0) {
                String cand = allKw.remove(0);
                if (!distractors.contains(cand))
                    distractors.add(cand);
            }
            // 選択肢セット
            java.util.List<String> choiceList = new java.util.ArrayList<>();
            choiceList.add(answer);
            for (int i = 0; i < 3; i++)
                choiceList.add(distractors.get(i));
            java.util.Collections.shuffle(choiceList);
            for (int i = 0; i < 4; i++)
                choices[i] = choiceList.get(i);
        } catch (Exception e) {
            // エラー内容をHTMLに出力
            StringBuilder errMsg = new StringBuilder();
            errMsg.append("APIからクイズを取得できませんでした。<br>");
            errMsg.append("<details><summary>エラー詳細を表示</summary><pre>");
            errMsg.append(e.toString()).append("\n");
            if (e.getMessage() != null)
                errMsg.append(e.getMessage()).append("\n");
            for (StackTraceElement ste : e.getStackTrace()) {
                errMsg.append(ste.toString()).append("\n");
            }
            errMsg.append("</pre></details>");
            question = errMsg.toString();
            answer = "エラー";
            choices = new String[] { "エラー", "", "", "" };
            e.printStackTrace(); // 標準出力にも出す
        }
        // シャッフル
        java.util.List<String> list = java.util.Arrays.asList(choices);
        java.util.Collections.shuffle(list);
        choices = list.toArray(new String[0]);
        int correctIdx = 0;
        for (int i = 0; i < 4; i++) {
            if (choices[i].equals(answer))
                correctIdx = i;
        }
        StringBuilder html = new StringBuilder();
        html.append("<div id='historyQuiz'>");
        html.append("<h2>歴史クイズ</h2>");
        // 問題文を角丸枠で強調
        html.append("<div class='question-box'>問題：" + question + "</div>");
        for (int i = 0; i < 4; i++) {
            html.append("<button onclick=\"checkHistoryAnswer(" + correctIdx + "," + i
                    + ")\" style='font-size:30px; margin:10px;'>" + choices[i] + "</button><br>");
        }
        html.append("<div id='historyResult'></div>");
        // 「新しい問題を取得」ボタンを追加
        html.append("<button class='home-btn' onclick='showNextHistoryQuiz();' style='margin-right:20px;'>新しい問題を取得</button>");
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
                // クイズ部分だけ再生成する関数
                "function showNextHistoryQuiz() {\n" +
                "  location.href = location.href;\n" +
                "}\n" +
                "</script>");
        return html.toString();
    }

    // 2つの文字列のLevenshtein距離を計算
    private static int getLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++)
            dp[i][0] = i;
        for (int j = 0; j <= s2.length(); j++)
            dp[0][j] = j;
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1]));
                }
            }
        }
        return dp[s1.length()][s2.length()];
    }
}
