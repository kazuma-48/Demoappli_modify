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
        String enWord = fetchEnglish(jaWord);
        if (enWord == null || enWord.isEmpty())
            enWord = "example";
        String question = "次の英単語の日本語訳はどれ？\n" + enWord;
        List<String> choices = new ArrayList<>();
        int correctIdx = 0;
        try {
            String apikey = System.getenv("GEMINI_API_KEY");
            if (apikey == null)
                throw new Exception("APIキー未設定");
            // Gemini APIで選択肢（日本語訳4つ、正解含む）を生成
            String prompt = "The correct Japanese translation for the English word '" + enWord + "' is '" + jaWord
                    + "'. " +
                    "Create a 4-choice Japanese quiz. Output JSON: {choices: string[], correctIdx: number}. Only output JSON.";
            String response = QuizApp.GeminiClient.queryGemini(prompt, apikey);
            int cIdx = response.indexOf("choices");
            int iIdx = response.indexOf("correctIdx");
            if (cIdx != -1 && iIdx != -1) {
                int cStart = response.indexOf('[', cIdx);
                int cEnd = response.indexOf(']', cStart);
                String[] arr = response.substring(cStart + 1, cEnd).replaceAll("\"", "").split(",");
                for (String s : arr)
                    choices.add(s.trim());
                int iStart = response.indexOf(':', iIdx) + 1;
                int iEnd = response.indexOf('}', iStart);
                correctIdx = Integer.parseInt(response.substring(iStart, iEnd).replaceAll("[^0-9]", "").trim());
                // 選択肢をランダムに並び替え、正解インデックスを再計算
                String correctAnswer = choices.get(correctIdx);
                Collections.shuffle(choices);
                correctIdx = choices.indexOf(correctAnswer);
            } else {
                // フォールバック: 手動生成
                choices.clear();
                choices.add(jaWord);
                for (int i = 0; i < 3; i++)
                    choices.add("ダミー訳" + (i + 1));
                Collections.shuffle(choices);
                correctIdx = choices.indexOf(jaWord);
            }
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

    // Wiktionary APIで日本語→英語翻訳を取得（langlinks配列を直接探す簡易パース）
    private static String fetchEnglishFromWiktionary(String jaWord) {
        try {
            String urlStr = "https://ja.wiktionary.org/w/api.php?action=query&titles="
                    + URLEncoder.encode(jaWord, "UTF-8") + "&prop=langlinks&lllang=en&format=json";
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
                // langlinks配列から"*":"英単語"を探す
                int llIdx = res.indexOf("\"langlinks\":");
                if (llIdx != -1) {
                    int starIdx = res.indexOf("\"*\":\"", llIdx);
                    if (starIdx != -1) {
                        int start = starIdx + 6;
                        int end = res.indexOf('"', start);
                        if (end > start) {
                            String word = res.substring(start, end);
                            // 意味のない文字列の場合は除外
                            if (word.matches("[a-zA-Z -]+")) {
                                return word;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // エラー内容を出力
        }
        return "No translation found";
    }

    // Google翻訳（非公式API）で日本語→英語翻訳を取得
    public static String fetchEnglishFromGoogle(String jaWord) {
        try {
            String urlStr = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=ja&tl=en&dt=t&q="
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
                // [[["英訳","原文",,,],...],...]
                int quote1 = res.indexOf('"');
                int quote2 = res.indexOf('"', quote1 + 1);
                if (quote1 != -1 && quote2 != -1 && quote2 > quote1) {
                    String word = res.substring(quote1 + 1, quote2);
                    if (word.matches("[a-zA-Z -]+")) {
                        return word;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Wiktionary→Google翻訳の順で英訳を取得
    private static String fetchEnglish(String jaWord) {
        String word = fetchEnglishFromWiktionary(jaWord);
        if (word == null || word.equals("No translation found") || word.isEmpty()) {
            word = fetchEnglishFromGoogle(jaWord);
        }
        return word;
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
