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
            Set<String> uniqueTitles = new LinkedHashSet<>();
            List<String> pickedTitles = new ArrayList<>();
            // 各RSSから最低1件ずつピックアップ
            for (String rssUrl : rssUrls) {
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(rssUrl))
                            .build();
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    String xml = response.body();
                    int idx = 0;
                    List<String> localTitles = new ArrayList<>();
                    while ((idx = xml.indexOf("<title>", idx)) != -1) {
                        int start = idx + "<title>".length();
                        int end = xml.indexOf("</title>", start);
                        if (end == -1)
                            break;
                        String title = xml.substring(start, end).trim();
                        if (!title.equals("Yahoo!ニュース")) {
                            localTitles.add(title);
                        }
                        idx = end + "</title>".length();
                    }
                    // そのRSSから1件ランダムに選ぶ
                    if (!localTitles.isEmpty()) {
                        Collections.shuffle(localTitles);
                        String pick = localTitles.get(0);
                        if (!uniqueTitles.contains(pick)) {
                            pickedTitles.add(pick);
                            uniqueTitles.add(pick);
                        }
                    }
                } catch (Exception ignore) {
                }
            }
            // さらに全タイトルから重複なしで追加し、4件以上にする
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
                        if (!title.equals("Yahoo!ニュース") && !uniqueTitles.contains(title)) {
                            pickedTitles.add(title);
                            uniqueTitles.add(title);
                        }
                        idx = end + "</title>".length();
                    }
                } catch (Exception ignore) {
                }
            }
            if (pickedTitles.size() < 4) {
                return new Quiz("ニュース記事が取得できませんでした。", new String[] { "-", "-", "-", "-" }, 0);
            }
            Collections.shuffle(pickedTitles);
            List<String> quizTitles = pickedTitles.subList(0, 4);
            // 1つ目の記事を問題文の元ネタに、残り3つ＋正解で選択肢
            String mainTitle = quizTitles.get(0);
            List<String> choices = new ArrayList<>(quizTitles);

            // GeminiAPIでSPI（総合適性検査）形式のクイズ問題文と選択肢を生成
            String prompt = "次のニュース記事タイトルをもとに、SPI（総合適性検査）のような形式で、文章読解型のクイズ問題文と4つの選択肢を日本語で自然に出力してください。問題文は一文の短いストーリーや状況説明とし、選択肢は内容理解や推論を問うものにしてください。解説文は不要です。タイトル: "
                    + mainTitle + " 選択肢: " + String.join(", ", choices);
            String geminiResult = GeminiClient.translate(prompt);

            // GeminiAPIの出力を分割（1行目:問題文, 2行目以降:選択肢）
            String[] lines = geminiResult.split("\n");
            String question = "クイズ";
            List<String> geminiChoices = new ArrayList<>();
            String correctAnswer = null;
            boolean inChoices = false;
            for (String line : lines) {
                String trimmed = line.trim();
                // 選択肢開始の目印（例: "1."や"①"など）
                if (!inChoices && !trimmed.isEmpty()) {
                    // 問題文から先頭番号や記号を除去
                    question = trimmed.replaceFirst("^[-・●\\d.\\s]*", "").trim();
                } else if (!inChoices && trimmed.isEmpty()) {
                    continue;
                } else if (!inChoices && (trimmed.matches("^([1-4]|[①-④]|[Ａ-ＤＡ-ＤA-Da-d]|[ア-エ])\\.|^[-・●]"))) {
                    inChoices = true;
                }
                if (inChoices && !trimmed.isEmpty()) {
                    String c = trimmed.replaceFirst("^[-・●\\d.\\s]*", "").trim();
                    if (!c.isEmpty())
                        geminiChoices.add(c);
                }
            }
            // Geminiの選択肢が4つ未満なら元のchoicesで補完
            while (geminiChoices.size() < 4) {
                for (String c : choices) {
                    if (!geminiChoices.contains(c) && geminiChoices.size() < 4) {
                        geminiChoices.add(c);
                    }
                }
            }
            // 正解候補をmainTitleから推定（タイトルに含まれる人名・団体名が選択肢にあればそれを正解とする）
            // Geminiの選択肢のうち、mainTitleに含まれる単語が最も多いものを正解とみなす
            int bestScore = -1;
            int bestIdx = 0;
            for (int i = 0; i < geminiChoices.size(); i++) {
                String choice = geminiChoices.get(i);
                int score = 0;
                for (String word : mainTitle.split("[\s　、,。・]")) {
                    if (!word.isEmpty() && choice.contains(word))
                        score++;
                }
                if (score > bestScore) {
                    bestScore = score;
                    bestIdx = i;
                }
            }
            // 選択肢をシャッフルし、正解インデックスを再計算
            List<String> shuffled = new ArrayList<>(geminiChoices);
            Collections.shuffle(shuffled);
            int correctIdx = shuffled.indexOf(geminiChoices.get(bestIdx));
            return new Quiz(question, shuffled.toArray(new String[0]), correctIdx);
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