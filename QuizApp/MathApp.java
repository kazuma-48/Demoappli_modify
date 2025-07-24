package QuizApp;

import java.util.Random;

/**
 * Geminiを呼び出すサンプルアプリ
 * 
 * 注意事項
 * あらかじめ環境変数 'GEMINI_API_KEY' にAPIキーを設定してから実行してください
 * 
 * @author 243203
 * @version 1.0
 */
public class MathApp {
    /**
     * メイン処理: Geminiに質問して、返答を標準出力に出力します
     * 
     * @param args コマンドライン引数(使用しない)
     */
    public static Quiz getQuiz() {
        try{
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("APIキーが設定されていません。環境変数:GEMINI_API_KEY を設定してください。");
        }
        String[] keywords = {
                // 関数・代数（10）
                    "一次関数の計算", "二次関数の計算", "三次関数の計算", "指数関数の計算", "対数関数の計算",
                "絶対値関数の計算", "関数のグラフの計算", "関数の変域の計算", "関数の最大値の計算", "関数の最小値の計算",

                // 幾何・図形（10）
                "円の面積の計算", "円の体積の計算", "球の体積の計算", "円柱の体積の計算", "円錐の体積の計算",
                "三角形の面積の計算", "台形の面積の計算", "扇形の面積の計算", "角度の求め方の計算", "相似の証明の計算",

                // 式・方程式（10）
                "因数分解の計算", "平方完成の計算", "解の公式の計算", "連立方程式の計算", "一次方程式の計算",
                "二次方程式の計算", "不等式の計算", "文字式の計算", "式の展開の計算", "式の値の計算",

                // 数列・集合・論理（5）
                "等差数列の計算", "等比数列の計算", "集合の記号の計算", "命題と論理の計算", "真偽の判定の計算",

                // 確率・統計（5）
                "確率の計算", "場合の数の計算", "期待値の計算", "中央値の計算", "標準偏差の計算",

                // 微分・積分（5）
                "微分係数の計算", "導関数の計算", "接線の方程式の計算", "積分の計算", "面積の求積法の計算",

                // その他（5）
                "座標平面の計算", "ベクトルの計算", "行列の計算", "複素数の計算", "数学的帰納法の計算"
        };

        String quizContent = keywords[new Random().nextInt(keywords.length)];

        String prompt = "次の文章を四択のクイズ問題（question, choices, answer, explanation）としてJSON形式で出力してください。"
                + "\n文章: " + quizContent
                + "\n出力例: {\n"
                + "    \"question\": \"a^2 + 2ab + b^2 を因数分解するとどうなる？\",\n"
                + "    \"choices\": [\n"
                + "        \"(a + b)^2\",\n"
                + "        \"(a - b)^2\",\n"
                + "        \"a^2 - b^2\",\n"
                + "        \"a^2 + b^2\"\n"
                + "    ],\n"
                + "    \"answer\": \"(a + b)^2\",\n"
                + "    \"explanation\": \"a^2 + 2ab + b^2 を因数分解すると (a + b)^2 になります。\"\n"
                + "}\n"
                + "必ずJSONのみを出力してください。";

        String json = createQuizGemini.queryGemini(prompt, apiKey);
        String cleanJson = json.replace("```json", "").replace("```", "").trim();
        try {
            org.json.JSONObject jsonObject = new org.json.JSONObject(cleanJson);
            String question = jsonObject.getString("question");
            org.json.JSONArray choicesArray = jsonObject.getJSONArray("choices");
            String[] choices = new String[choicesArray.length()];
            for (int i = 0; i < choicesArray.length(); i++) {
                choices[i] = choicesArray.getString(i);
            }
            int correctIdx = -1;
            if (jsonObject.has("answer")) {
                Object answerObj = jsonObject.get("answer");

                if (answerObj instanceof String) {
                    String answer = (String) answerObj;
                    for (int i = 0; i < choices.length; i++) {
                        if (choices[i].equals(answer)) {
                            correctIdx = i;
                            break;
                        }
                    }
                } else if (answerObj instanceof Integer) {
                    correctIdx = (Integer) answerObj;
                } else {
                    throw new RuntimeException("answerの形式が不明です: " + answerObj);
                }
            }
            return new Quiz(question, choices, correctIdx);
        } catch (Exception e) {
            throw new RuntimeException("クイズの生成に失敗しました: " + e.getMessage(), e);
        }
    }catch(

    Exception e)
    {
        throw new RuntimeException("Gemini API呼び出しに失敗しました: " + e.getMessage(), e);
    }
    }
    
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