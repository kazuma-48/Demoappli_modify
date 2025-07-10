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
        } catch (Exception e) {
            System.out.println("エラーが発生しました: " + e.getMessage());
            e.printStackTrace();
        }
        scanner.close();
    }
}