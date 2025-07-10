package QuizApp.Gemini;
/**
 * Geminiを呼び出すサンプルアプリ
 * 
 * 注意事項:
 * あらかじめ環境変数'GEMINI_API_KEY'にAPIキーを設定してから実行ください。
 * 
 * @author n.katayama
 * @version 1.0
 */
public class GeminiSanple {
    /**
     * メイン処理: Geminiに質問して、出力します
     * 
     * @param args コマンドライン引数（使用しません）
     */
    public static void main(String[] args) throws Exception {
        String apikey = System.getenv("GEMINI_API_KEY");//環境変数から取得
        if(apikey == null){
            System.out.println("APIキーが設定されていません。環境変数 GEMINI_API_KEY を設定してください。");
            return;
        }

        String question = "授業によく遅刻します。どうしたらいいでしょう？";
        
        try{
            String answer = GeminiClient.queryGemini(question, apikey);
            System.out.println(answer);
        }catch(Exception e){
            System.out.println("エラーが発生しました: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
