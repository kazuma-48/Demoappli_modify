package QuizApp;

import java.util.*;
import org.json.*;

public class Music {
    // 出題済みタイトルを記録する（全問出題後はリセット）
    private static final Set<String> usedQuizTitles = new HashSet<>();

    // CLI用：問題文・選択肢・正解インデックスを返す
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

    // イントロクイズ用：問題文・選択肢・正解インデックス・プレビューURLを返す
    public static class IntroQuiz {
        public final String question;
        public final String[] choices;
        public final int correctIdx;

        public IntroQuiz(String question, String[] choices, int correctIdx) {
            this.question = question;
            this.choices = choices;
            this.correctIdx = correctIdx;
        }
    }

    public static Quiz getQuiz() throws Exception {
        // iTunes Search APIで楽曲を検索（日本語楽曲だが問題文に「日本」は表示しない）
        String apiUrl = "https://itunes.apple.com/search?term=%E6%97%A5%E6%9C%AC&media=music&entity=song&country=JP&limit=50";
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
        JSONObject json = new JSONObject(content.toString());
        JSONArray results = json.getJSONArray("results");
        // 4曲分のタイトル・アーティストを取得
        List<String> titles = new ArrayList<>();
        List<String> artists = new ArrayList<>();
        Set<Integer> usedIdx = new HashSet<>();
        Random rand = new Random();
        // 未出題タイトルのみ抽出
        int maxTries = results.length() * 2;
        int tries = 0;
        while (tries < maxTries) {
            titles.clear();
            artists.clear();
            usedIdx.clear();
            while (titles.size() < 4 && usedIdx.size() < results.length()) {
                int idx = rand.nextInt(results.length());
                if (usedIdx.contains(idx))
                    continue;
                usedIdx.add(idx);
                JSONObject trackObj = results.getJSONObject(idx);
                String title = trackObj.optString("trackName", "").replace("日本", "");
                String artist = trackObj.optString("artistName", "");
                if (!title.isEmpty() && !artist.isEmpty() && !titles.contains(title)) {
                    titles.add(title);
                    artists.add(artist);
                }
            }
            if (titles.size() < 4)
                break;
            int correctIdx = rand.nextInt(titles.size());
            String correctTitle = titles.get(correctIdx);
            if (!usedQuizTitles.contains(correctTitle)) {
                usedQuizTitles.add(correctTitle);
                String question = String.format("次の曲のアーティストは誰？\n『%s』", correctTitle);
                String[] choices = artists.toArray(new String[0]);
                return new Quiz(question, choices, correctIdx);
            }
            tries++;
        }
        // 全問出題済みならリセットして再出題
        usedQuizTitles.clear();
        // 再帰的に新しい問題を出す
        return getQuiz();
    }

    public static IntroQuiz getIntroQuiz() throws Exception {
        // iTunes Search APIで楽曲を検索（日本語楽曲だが問題文に「日本」は表示しない）
        String apiUrl = "https://itunes.apple.com/search?term=%E6%97%A5%E6%9C%AC&media=music&entity=song&country=JP&limit=50";
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
        JSONObject json = new JSONObject(content.toString());
        JSONArray results = json.getJSONArray("results");
        // 4曲分のタイトル・アーティストを取得
        List<String> titles = new ArrayList<>();
        List<String> artists = new ArrayList<>();
        Set<Integer> usedIdx = new HashSet<>();
        Random rand = new Random();
        // 未出題タイトルのみ抽出
        int maxTries = results.length() * 2;
        int tries = 0;
        while (tries < maxTries) {
            titles.clear();
            artists.clear();
            usedIdx.clear();
            while (titles.size() < 4 && usedIdx.size() < results.length()) {
                int idx = rand.nextInt(results.length());
                if (usedIdx.contains(idx))
                    continue;
                usedIdx.add(idx);
                JSONObject trackObj = results.getJSONObject(idx);
                String title = trackObj.optString("trackName", "").replace("日本", "");
                String artist = trackObj.optString("artistName", "");
                if (!title.isEmpty() && !artist.isEmpty() && !titles.contains(title)) {
                    titles.add(title);
                    artists.add(artist);
                }
            }
            if (titles.size() < 4)
                break;
            int correctIdx = rand.nextInt(titles.size());
            String correctTitle = titles.get(correctIdx);
            if (!usedQuizTitles.contains(correctTitle)) {
                usedQuizTitles.add(correctTitle);
                String question = String.format("次の曲のアーティストは誰？\n『%s』", correctTitle);
                String[] choices = artists.toArray(new String[0]);
                return new IntroQuiz(question, choices, correctIdx);
            }
            tries++;
        }
        // 全問出題済みならリセットして再出題
        usedQuizTitles.clear();
        // 再帰的に新しい問題を出す
        return getIntroQuiz();
    }
}