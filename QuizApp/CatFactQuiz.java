package QuizApp;
    
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONObject;

public class CatFactQuiz {

    // APIから猫の豆知識を取得するメソッド
    public static String getCatFact() throws Exception {
        String apiUrl = "https://catfact.ninja/fact";

        URL url = new URL(apiUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();

        if (responseCode == 200) {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // JSONレスポンスの解析
            JSONObject json = new JSONObject(response.toString());
            return json.getString("fact");
        } else {
            return null;
        }
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("=== 猫豆知識クイズ ===");

            // 猫の豆知識を1つ取得
            String fact = getCatFact();

            if (fact == null) {
                System.out.println("猫の豆知識を取得できませんでした。");
                return;
            }

            // ここではシンプルに豆知識の一部を表示してクイズにする例
            System.out.println("次の文は猫の豆知識の一部です。");
            System.out.println("問題: この文は正しいでしょうか？（yes/no）");

            // たとえば、クイズとして「豆知識全文の長さが30文字以上か？」みたいな質問にする
            boolean isLongFact = fact.length() >= 30;

            System.out.println("豆知識の一部: " + fact.substring(0, Math.min(20, fact.length())) + "...");

            // ユーザー回答受付
            String answer = scanner.nextLine().trim().toLowerCase();

            boolean userAnswer = answer.equals("yes") || answer.equals("y");

            // 正解判定
            if (userAnswer == isLongFact) {
                System.out.println("正解！豆知識全文: " + fact);
            } else {
                System.out.println("不正解...豆知識全文: " + fact);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
