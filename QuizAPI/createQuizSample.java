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

        System.out.print("クイズにしたい内容を入力してください（問題文は入力せずテーマや知識のみを入力してください。）: ");
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        String quizContent = scanner.nextLine();

        // Geminiに四択クイズのJSON形式で作成するよう指示する
        String prompt = "次の文章を四択のクイズ問題（question, choices, answer, explanation）としてJSON形式で出力してください。"
                + "\n文章: " + quizContent
                + "\n出力例: {\n"
                + "    \"question\": \"鎌倉幕府を開いた人物は誰ですか？\",\n"
                + "    \"choices\": [\n"
                + "        \"源頼朝\",\n"
                + "        \"織田信長\",\n"
                + "        \"徳川家康\",\n"
                + "        \"足利尊氏\"\n"
                + "    ],\n"
                + "    \"answer\": \"源頼朝\",\n"
                + "    \"explanation\": \"鎌倉幕府は1192年に源頼朝によって開かれました。日本で最初の武家政権です。\"\n"
                + "}\n"
                + "必ずJSONのみを出力してください。";

        try {
            String answer = createQuizGemini.queryGemini(prompt, apiKey);
            System.out.println(answer);

            // 保存するかどうかを確認
            System.out.print("このクイズをquiz.jsonに保存しますか？（1: はい / 2: いいえ）: ");
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
        scanner.close();
    }
}