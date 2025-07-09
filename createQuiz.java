/**
 * Geminiを呼び出すサンプルアプリ
 * 
 * 注意事項
 * あらかじめ環境変数 'GEMINI_API_KEY' にAPIキーを設定してから実行してください
 * 
 * @author 243203
 * @version 1.0
 */

public class createQuiz {
    /**
     * メイン処理: Geminiに質問して、返答を標準出力に出力します
     * 
     * @param args コマンドライン引数(使用しない)
     */
    public static void main(String[] args)  throws Exception{
        // GeminiのAPIキーを環境変数から取得
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null ) {
            System.out.println("APIキーが設定されていません。環境変数:GEMINI_API_KEY を設定してください。");
            return;
        }

        String question = "問題を自作する";
        try{
            String answer = createQuizGemini.queryGemini(question, apiKey);
            System.out.println(answer);
        } catch (Exception e) {
            System.out.println("エラーが発生しました: " + e.getMessage());
        }
    }
}