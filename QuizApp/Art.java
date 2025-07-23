package QuizApp;

public class Art {
    // クイズを生成するメソッド
    public static Quiz getQuiz() {
        try {
            // Art Institute of Chicago APIから作品データ取得
            String apiUrl = "https://api.artic.edu/api/v1/artworks?page=1&limit=100";
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) java.net.URI.create(apiUrl).toURL()
                    .openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            java.util.List<String> titles = new java.util.ArrayList<>();
            java.util.List<String> artists = new java.util.ArrayList<>();
            try (java.io.BufferedReader br = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                org.json.JSONObject jsonResponse = new org.json.JSONObject(response.toString());
                org.json.JSONArray data = jsonResponse.optJSONArray("data");
                if (data != null && data.length() > 0) {
                    for (int i = 0; i < data.length(); i++) {
                        org.json.JSONObject artwork = data.getJSONObject(i);
                        String title = artwork.optString("title", "");
                        String artist = artwork.optString("artist_title", "");
                        if (!title.isEmpty() && !artist.isEmpty()) {
                            titles.add(title);
                            artists.add(artist);
                        }
                    }
                }
            }
            if (titles.isEmpty() || artists.isEmpty()) {
                String[] choices = { "エラー", "", "", "" };
                return new Quiz("APIからクイズを取得できませんでした。", choices, 0);
            }
            // ランダムで1つ選択
            int idx = (int) (Math.random() * titles.size());
            String artworkTitle = titles.get(idx);
            String artistName = artists.get(idx);
            // 日本語翻訳（ダミー）
            String jpArtworkTitle = translateToJapanese(artworkTitle);
            String jpArtistName = translateToJapanese(artistName);
            // ダミー作成者名を生成（本来はGeminiAPIで生成）
            java.util.List<String> dummyArtists = getDummyArtists(artistName);
            java.util.LinkedHashSet<String> choicesSet = new java.util.LinkedHashSet<>();
            choicesSet.add(jpArtistName);
            for (String dummy : dummyArtists) {
                choicesSet.add(translateToJapanese(dummy));
            }
            java.util.List<String> choicesList = new java.util.ArrayList<>(choicesSet);
            java.util.Collections.shuffle(choicesList);
            String[] choices = choicesList.toArray(new String[0]);
            int correctIdx = 0;
            for (int i = 0; i < choices.length; i++) {
                if (choices[i].equals(jpArtistName))
                    correctIdx = i;
            }
            String question = "次の作品の作者は誰ですか？: 『" + jpArtworkTitle + "』";
            return new Quiz(question, choices, correctIdx);
        } catch (Exception e) {
            String[] choices = { "エラー", "", "", "" };
            return new Quiz("APIからクイズを取得できませんでした。", choices, 0);
        }
    }

    // ダミー作成者名生成（本来はGeminiAPIで生成）
    private static java.util.List<String> getDummyArtists(String correctArtist) {
        java.util.List<String> dummies = new java.util.ArrayList<>();
        java.util.Random rand = new java.util.Random();
        for (int i = 0; i < 3; i++) {
            String dummy = correctArtist;
            // 1文字だけランダムで置換
            if (dummy.length() > 2) {
                int pos = rand.nextInt(dummy.length());
                char c = (char) ('A' + rand.nextInt(26));
                dummy = dummy.substring(0, pos) + c + dummy.substring(pos + 1);
            }
            // 既存のダミーや正解と重複しないように
            if (!dummies.contains(dummy) && !dummy.equals(correctArtist)) {
                dummies.add(dummy);
            } else {
                i--; // 重複したらやり直し
            }
        }
        return dummies;
    }

    // Google翻訳APIを使った英語→日本語翻訳
    private static String translateToJapanese(String text) {
        if (text == null || text.isEmpty())
            return "";
        try {
            String apiUrl = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=ja&dt=t&q="
                    + java.net.URLEncoder.encode(text, "UTF-8");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) java.net.URI.create(apiUrl).toURL()
                    .openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            try (java.io.BufferedReader br = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                // レスポンスは多次元配列: [[["日本語訳", "原文", ...], ...], ...]
                org.json.JSONArray arr = new org.json.JSONArray(response.toString());
                return arr.getJSONArray(0).getJSONArray(0).getString(0);
            }
        } catch (Exception e) {
            // 失敗時は元のテキスト
            return text;
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
