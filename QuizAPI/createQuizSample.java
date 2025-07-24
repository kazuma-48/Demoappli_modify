package QuizAPI;

/**
 * Geminiを呼び出すサンプルアプリ
 * 
 * 注意事項
 * あらかじめ環境変数 'GEMINI_API_KEY' にAPIキーを設定してから実行してください
 * 
 * @author 243203
 * @version 1.0
 */
public class createQuizSample {
    /**
     * メイン処理: Geminiに質問して、返答を標準出力に出力します
     * 
     * @param args コマンドライン引数(使用しない)
     */
    public static void main(String[] args) throws Exception {
        String apiKey = System.getenv("GEMINI_API_KEY"); // GeminiのAPIキーを環境変数から取得
        if (apiKey == null) {
            System.out.println("APIキーが設定されていません。環境変数:GEMINI_API_KEY を設定してください。");
            return;
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
                "等差数列の計算", "等比数列の計算", "集2合の記号の計算", "命題と論理の計算", "真偽の判定の計算",

                // 確率・統計（5）
                "確率の計算", "場合の数の計算", "期待値の計算", "中央値の計算", "標準偏差の計算",

                // 微分・積分（5）
                "微分係数の計算", "導関数の計算", "接線の方程式の計算", "積分の計算", "面積の求積法の計算",

                // その他（5）
                "座標平面の計算", "ベクトルの計算", "行列の計算", "複素数の計算", "数学的帰納法の計算"
        };

        // System.out.print("クイズにしたい内容を入力してください（問題文は入力せずテーマや知識のみを入力してください。）: ");
        // java.util.Scanner scanner = new java.util.Scanner(System.in);
        String quizContent = keywords[(int) (Math.random() * keywords.length)];

        // Geminiに四択クイズのJSON形式で作成するよう指示する
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

        try {
            String answer = createQuizGemini.queryGemini(prompt, apiKey);
            System.out.println(answer);

            // 保存するかどうかを確認
            System.out.print("このクイズをquiz.jsonに保存しますか？（1: はい / 2: いいえ）: ");
            java.util.Scanner scanner = new java.util.Scanner(System.in);
            String save = scanner.nextLine().trim();
            if (save.equals("1")) {
                java.nio.file.Path path = java.nio.file.Paths.get("QuizAPI", "quiz.json");
                java.nio.file.Files.createDirectories(path.getParent());

                // answerから不要な文字列を除去
                String cleanAnswer = answer.replace("```json", "").replace("```", "").trim();

                String content;
                if (java.nio.file.Files.exists(path)) {
                    // 既存ファイルを読み込む
                    content = java.nio.file.Files.readString(path, java.nio.charset.StandardCharsets.UTF_8);
                    // 末尾の ] や不要な文字列を除去
                    int arrayEnd = content.lastIndexOf(']');
                    if (arrayEnd != -1) {
                        content = content.substring(0, arrayEnd);
                    }
                    content = content.replace("```json", "").replace("```", "").trim();
                    // 配列の中身が空でない場合はカンマを追加
                    if (content.length() > 1) {
                        content = content + ",";
                    } else {
                        content = "[";
                    }
                    // 新しい問題を追加
                    content = content + "\n" + cleanAnswer + "\n]";
                    // 保存
                    java.nio.file.Files.writeString(path, content, java.nio.charset.StandardCharsets.UTF_8);
                } else {
                    // 新規作成
                    content = "[\n" + cleanAnswer + "\n]";
                    java.nio.file.Files.writeString(path, content, java.nio.charset.StandardCharsets.UTF_8);
                }
                System.out.println("QuizAPI/quiz.jsonに保存しました。");
            } else {
                System.out.println("クイズ作成を終了します。");
            }
        } catch (Exception e) {
            System.out.println("エラーが発生しました: " + e.getMessage());
            e.printStackTrace();
        }
        // scanner.close();
    }
}