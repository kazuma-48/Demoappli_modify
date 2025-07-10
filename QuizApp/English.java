package QuizApp;

import java.util.*;
import java.net.*;
import java.io.*;

public class English {
    // 50単語リスト
    private static final String[] WORDS = {
            "最近", "情報", "革命", "衣料品", "命令", "実験", "緊急", "物体", "知識", "政府",
            "距離", "印象", "病気", "冷蔵庫", "発見", "到着", "態度", "税金", "記事", "災害",
            "人気", "決定", "幼少期", "腹", "銀行口座", "執行", "研究", "力", "社長", "財産",
            "責任", "顧客", "鉄", "100万", "言語", "天才", "信用", "借金", "友達", "文化",
            "表現", "職業", "恐怖", "人口", "変化", "配達", "ウイルス", "生き物", "索引", "独立"
    };

    // CLI用：問題文・選択肢・正解インデックスを返す
    public static Quiz getQuiz() {
        Random rand = new Random();
        String jaWord = WORDS[rand.nextInt(WORDS.length)];
        String enWord = fetchEnglishFromGlosbe(jaWord);
        if (enWord == null || enWord.isEmpty())
            enWord = "example";
        String question = "次の英単語の日本語訳はどれ？\n" + enWord;
        List<String> choices = new ArrayList<>();
        int correctIdx = 0;
        try {
            String apikey = System.getenv("GEMINI_API_KEY");
            if (apikey == null)
                throw new Exception("APIキー未設定");
            // Gemini APIでダミー日本語訳を取得
            choices.add(jaWord); // 正解
            for (int i = 0; i < 3; i++) {
                String prompt = "Give me a Japanese word that is a plausible but incorrect translation for the English word '"
                        + enWord + "'. Only output the word.";
                String dummy = QuizApp.Gemini.GeminiClient.queryGemini(prompt, apikey).replaceAll("[\n\r]", "").trim();
                if (!choices.contains(dummy))
                    choices.add(dummy);
            }
            while (choices.size() < 4) {
                String fallback = "ダミー訳" + (choices.size() + 1);
                if (!choices.contains(fallback))
                    choices.add(fallback);
            }
            Collections.shuffle(choices);
            correctIdx = choices.indexOf(jaWord);
        } catch (Exception e) {
            // エラー時は手動生成
            choices.clear();
            choices.add(jaWord);
            for (int i = 0; i < 3; i++)
                choices.add("ダミー訳" + (i + 1));
            Collections.shuffle(choices);
            correctIdx = choices.indexOf(jaWord);
        }
        return new Quiz(question, choices.toArray(new String[0]), correctIdx);
    }

    // 5文字のランダム英単語生成
    private static String randomDummyWord(Random rand) {
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append(alphabet.charAt(rand.nextInt(alphabet.length())));
        }
        return sb.toString();
    }

    // Glosbe APIで日本語→英語翻訳を取得
    private static String fetchEnglishFromGlosbe(String jaWord) {
        try {
            String urlStr = "https://glosbe.com/gapi/translate?from=ja&dest=en&format=json&phrase="
                    + URLEncoder.encode(jaWord, "UTF-8");
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();
                String res = response.toString();
                int tIdx = res.indexOf("\\\"phrase\\\":\\\"");
                if (tIdx == -1)
                    tIdx = res.indexOf("\"phrase\":\""); // 念のため両方対応
                if (tIdx != -1) {
                    int start = tIdx + 9;
                    int end = res.indexOf('"', start);
                    if (end > start) {
                        return res.substring(start, end);
                    }
                }
            }
        } catch (Exception e) {
            // エラー時はnull返却
        }
        return null;
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
