import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class News {
    // Yahoo!ニュースRSSから記事タイトルを取得し、GeminiAPIでクイズを生成
    public static Quiz getQuiz() {
        String rssUrl = "https://news.yahoo.co.jp/rss/topics/top-picks.xml";
        try {
            HttpClient client = HttpClient.newHttpClient();
            List<String> titles = new ArrayList<>();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(rssUrl))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String xml = response.body();
            int idx = 0;
            while ((idx = xml.indexOf("<title>", idx)) != -1) {
                int start = idx + "<title>".length();
                int end = xml.indexOf("</title>", start);
                if (end == -1)
                    break;
                String title = xml.substring(start, end).trim();
                if (!title.equals("Yahoo!ニュース")) {
                    titles.add(title);
                }
                idx = end + "</title>".length();
            }
            if (titles.isEmpty()) {
                return new Quiz("ニュース記事が取得できませんでした。", new String[] { "-", "-", "-", "-" }, 0);
            }
            Collections.shuffle(titles);
            String pickedTitle = titles.get(0);
            String articleContent = fetchArticleContent(pickedTitle); // ダミー関数
            // GeminiAPIに記事内容を渡して1問のクイズ（選択肢4つ）を生成
            String prompt = "次のニュース記事内容をもとに、問題文は記事内容を要約したストーリーや状況説明で問いかけるものとして、選択肢は4つ、その中に登場する人物名・日付・国名などを当てる形式にしてください。"
                    +
                    "また、「ある場所」などと濁さずに正式に表示してください。正解以外の選択肢（誤答）は、正解と間違えやすい実在の人物・日付・国名などを用意してください。問題文や選択肢には記事内容やタイトルを直接表示しないでください。解説文、問題、選択肢という記載は不要です。\n記事内容: "
                    + articleContent;
            String geminiResult = GeminiClient.translate(prompt);
            // GeminiAPIの出力を分割（1行目:問題文, 2行目以降:選択肢）
            String[] lines = geminiResult.split("\n");
            String question = null;
            List<String> geminiChoices = new ArrayList<>();
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty())
                    continue;
                if (question == null) {
                    question = trimmed.replaceFirst("^[-・●\\d.\\s]*", "").trim();
                } else {
                    String c = trimmed.replaceFirst("^[-・●\\d.\\s]*", "").trim();
                    if (!c.isEmpty())
                        geminiChoices.add(c);
                }
            }
            while (geminiChoices.size() < 4) {
                geminiChoices.add("-");
            }
            // 正解選択肢を自動判定（「（正解）」や「※正解」などの表記があればそれを優先）
            int correctIdx = 0;
            for (int i = 0; i < geminiChoices.size(); i++) {
                String c = geminiChoices.get(i);
                if (c.contains("正解") || c.contains("（正解") || c.contains("※正解") || c.matches(".*[\\(（]正解[\\)）].*")) {
                    correctIdx = i;
                    // 表記を除去
                    geminiChoices.set(i,
                            c.replaceAll("[（\\(※]?正解[\\)）]?$", "").replaceAll("[（\\(※]?正解[\\)）]?", "").trim());
                    break;
                }
            }
            if (question == null)
                question = "クイズ";
            // 選択肢をランダムにシャッフルし、正解インデックスを再計算
            List<String> shuffled = new ArrayList<>(geminiChoices);
            Collections.shuffle(shuffled);
            int shuffledCorrectIdx = shuffled.indexOf(geminiChoices.get(correctIdx));
            return new Quiz(question, shuffled.toArray(new String[0]), shuffledCorrectIdx);
        } catch (Exception e) {
            return new Quiz("ニュース記事が取得できませんでした。", new String[] { "-", "-", "-", "-" }, 0);
        }
    }

    // クイズ情報を保持するクラス
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

    // ニュースタイトルから記事内容を取得するダミー関数（実装例: タイトルをそのまま返す）
    private static String fetchArticleContent(String title) {
        // 本来はタイトルから記事本文を取得する処理を実装
        // ここではダミーでタイトルを返す
        return title;
    }
}