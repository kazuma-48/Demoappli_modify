import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class News {
    // Yahoo!ニュースRSSから記事タイトルを取得し、GeminiAPIでクイズを生成
    public static Quiz getQuiz() {
        String[] rssUrls = {
                "https://news.yahoo.co.jp/rss/topics/top-picks.xml",
                "https://news.yahoo.co.jp/rss/topics/sports.xml",
                "https://news.yahoo.co.jp/rss/topics/entertainment.xml",
                "https://news.yahoo.co.jp/rss/topics/business.xml",
                "https://news.yahoo.co.jp/rss/topics/world.xml",
                "https://news.yahoo.co.jp/rss/topics/science.xml",
                "https://news.yahoo.co.jp/rss/topics/local.xml"
        };
        try {
            HttpClient client = HttpClient.newHttpClient();
            List<String> titles = new ArrayList<>();
            for (String rssUrl : rssUrls) {
                try {
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
                } catch (Exception ignore) {
                }
            }
            if (titles.size() < 4) {
                return new Quiz("ニュース記事が取得できませんでした。", new String[] { "-", "-", "-", "-" }, 0);
            }
            Collections.shuffle(titles);
            List<String> quizTitles = titles.subList(0, 4);
            // 1つ目の記事を問題文の元ネタに、残り3つ＋正解で選択肢
            String mainTitle = quizTitles.get(0);
            List<String> choices = new ArrayList<>(quizTitles);

            // GeminiAPIでSPI（総合適性検査）形式のクイズ問題文と選択肢を生成
            String prompt = "次のニュース記事タイトルをもとに、SPI（総合適性検査）のような形式で、文章読解型のクイズ問題文と4つの選択肢を日本語で自然に出力してください。問題文は一文の短いストーリーや状況説明とし、選択肢は内容理解や推論を問うものにしてください。解説文は不要です。タイトル: "
                    + mainTitle + " 選択肢: " + String.join(", ", choices);
            String geminiResult = GeminiClient.translate(prompt);

            // GeminiAPIの出力を分割（1行目:問題文, 2行目以降:選択肢）
            String[] lines = geminiResult.split("\n");
            String question = lines.length > 0 ? lines[0] : "クイズ";
            List<String> geminiChoices = new ArrayList<>();
            for (int i = 1; i < lines.length; i++) {
                String c = lines[i].replaceAll("^[-・●\\d.\\s]*", "").trim();
                if (!c.isEmpty())
                    geminiChoices.add(c);
            }
            // Geminiの選択肢が4つ未満なら元のchoicesで補完
            while (geminiChoices.size() < 4) {
                for (String c : choices) {
                    if (!geminiChoices.contains(c) && geminiChoices.size() < 4) {
                        geminiChoices.add(c);
                    }
                }
            }
            // 選択肢をシャッフルし、正解インデックスをランダムに
            Collections.shuffle(geminiChoices);
            int correctIdx = new Random().nextInt(4);
            return new Quiz(question, geminiChoices.toArray(new String[0]), correctIdx);
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
}