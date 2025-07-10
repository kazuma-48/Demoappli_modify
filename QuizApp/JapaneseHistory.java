package QuizApp;

public class JapaneseHistory {
    // CLI用：問題文・選択肢・正解インデックスを返す
    public static Quiz getQuiz() {
        try {
            String[] keywords = {
                    "戦国", "幕府", "天皇", "藩", "将軍", "条約", "大名", "朝廷", "維新", "武士", "元号",
                    "藩主", "公家", "摂関", "征夷大将軍", "幕末", "明治時代", "江戸時代", "鎌倉時代", "室町時代", "安土桃山時代", "奈良時代", "平安時代", "飛鳥時代",
                    "古墳時代", "弥生時代", "縄文時代", "士族", "豪族", "藩士", "藩校", "藩政", "藩領", "藩医",
                    "摂政", "関白", "幕臣", "外様大名", "譜代大名", "親藩", "旗本", "御家人", "直参", "陪臣", "家老", "奉行", "目付", "町奉行", "寺社奉行",
                    "勘定奉行", "老中", "大老", "若年寄", "三奉行", "五奉行", "三職", "五摂家", "公卿", "殿様", "姫君", "侍女", "家臣", "家系", "家紋",
                    "家督", "家伝", "家法", "家訓", "家格", "家柄", "家名", "家宝", "家臣団", "家老職", "家老会議", "藩政改革", "藩札", "藩邸", "藩領",
                    "藩主家", "藩士団", "藩医", "藩学", "藩財政", "藩政期", "藩政時代", "藩政改革", "藩政危機", "藩政崩壊", "藩政末期", "藩政維新",
                    "聖徳太子", "織田信長", "豊臣秀吉", "徳川家康", "足利尊氏", "源頼朝", "平清盛", "卑弥呼", "聖武天皇", "紫式部", "藤原道長", "北条政子", "北条時宗",
                    "明智光秀", "石田三成", "西郷隆盛", "大久保利通", "坂本龍馬", "勝海舟", "伊藤博文", "山県有朋", "板垣退助", "木戸孝允", "近藤勇", "土方歳三",
                    "沖田総司", "新選組", "会津藩", "薩摩藩", "長州藩", "土佐藩", "水戸藩", "加賀藩", "仙台藩", "福岡藩", "熊本藩", "佐賀藩", "庄内藩", "米沢藩",
                    "彦根藩", "岡山藩", "姫路藩", "松江藩", "鳥取藩", "高知藩", "宇和島藩", "松山藩", "徳島藩", "高松藩", "丸亀藩", "今治藩", "大洲藩", "小倉藩",
                    "久留米藩", "柳川藩", "佐土原藩", "鹿児島藩", "島津家", "毛利家", "伊達家", "上杉家", "武田家", "北条家", "細川家", "前田家", "黒田家", "鍋島家",
                    "島原の乱", "応仁の乱", "承久の乱", "元寇", "関ヶ原の戦い", "大坂の陣", "戊辰戦争", "西南戦争", "日清戦争", "日露戦争", "太平洋戦争", "満州事変",
                    "二・二六事件", "五・一五事件", "大政奉還", "廃藩置県", "版籍奉還", "明治維新", "文明開化", "富国強兵", "殖産興業", "学制", "徴兵令", "地租改正",
                    "廃仏毀釈", "自由民権運動", "大日本帝国憲法", "日英同盟", "日独伊三国同盟", "ポツダム宣言", "東京裁判", "日本国憲法", "サンフランシスコ講和条約"
            };
            java.util.List<String> wordList = java.util.Arrays.asList(keywords);
            java.util.Collections.shuffle(wordList);
            String answer = wordList.get(0);
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
