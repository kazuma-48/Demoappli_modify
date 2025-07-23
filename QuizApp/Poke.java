package QuizApp;

import java.net.*;
import java.io.*;
import java.util.*;

public class Poke {
    // PokéAPIからポケモン名（英語）と種族値を取得し、日本語訳も付与するクイズ
    public static Quiz getQuiz() {
        try {
            int pokeId = 1 + new Random().nextInt(898); // 第8世代まで
            String apiUrl = "https://pokeapi.co/api/v2/pokemon/" + pokeId;
            String json = fetch(apiUrl);
            // 英語名取得
            String nameEn = extract(json, "\"name\":\"", "\"");
            // 日本語名取得（speciesエンドポイント）
            String nameJp = getJapaneseName(pokeId);
            // 英語名をEnglish.javaのAPIで日本語訳
            String nameJaTrans = English.fetchEnglishFromGoogle(nameEn);
            // 種族値リスト取得
            List<String> statNames = Arrays.asList("hp", "attack", "defense", "special-attack", "special-defense",
                    "speed");
            List<Integer> stats = new ArrayList<>();
            for (String stat : statNames) {
                stats.add(extractStat(json, stat));
            }
            StringBuilder correctSb = new StringBuilder();
            for (int i = 0; i < statNames.size(); i++) {
                correctSb.append(toJapanese(statNames.get(i))).append(": ").append(stats.get(i));
                if (i < statNames.size() - 1)
                    correctSb.append(", ");
            }
            String correctChoice = correctSb.toString();

            // ダミー選択肢生成（他のランダムなポケモン）
            List<String> wrongChoices = new ArrayList<>();
            Set<Integer> usedIds = new HashSet<>();
            usedIds.add(pokeId);
            while (wrongChoices.size() < 3) {
                int otherId = 1 + new Random().nextInt(898);
                if (usedIds.contains(otherId))
                    continue;
                usedIds.add(otherId);
                String otherJson = fetch("https://pokeapi.co/api/v2/pokemon/" + otherId);
                List<Integer> otherStats = new ArrayList<>();
                for (String stat : statNames) {
                    otherStats.add(extractStat(otherJson, stat));
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < statNames.size(); i++) {
                    sb.append(toJapanese(statNames.get(i))).append(": ").append(otherStats.get(i));
                    if (i < statNames.size() - 1)
                        sb.append(", ");
                }
                wrongChoices.add(sb.toString());
            }

            // 選択肢をシャッフル
            List<String> allChoices = new ArrayList<>();
            allChoices.add(correctChoice);
            allChoices.addAll(wrongChoices);
            Collections.shuffle(allChoices);
            int correctIdx = allChoices.indexOf(correctChoice);

            String question = "ポケモン『" + nameJp + "（" + nameEn + ", " + nameJaTrans + "）』の種族値は？";
            return new Quiz(question, allChoices.toArray(new String[0]), correctIdx);
        } catch (Exception e) {
            return new Quiz("APIエラー: " + e.getMessage(), new String[] { "-", "-", "-", "-" }, 0);
        }
    }

    // APIからJSON文字列取得
    private static String fetch(String urlStr) throws IOException {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URI(urlStr).toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null)
                    sb.append(line);
                return sb.toString();
            }
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URI syntax: " + urlStr, e);
        }
    }

    // JSONから種族値を抽出
    private static int extractStat(String json, String statName) {
        String key = "\"stat\":{\"name\":\"" + statName + "\"";
        int idx = json.indexOf(key);
        if (idx == -1)
            return -1;
        int baseIdx = json.lastIndexOf("base_stat", idx);
        if (baseIdx == -1)
            return -1;
        int colon = json.indexOf(":", baseIdx);
        int comma = json.indexOf(",", colon);
        String num = json.substring(colon + 1, comma).replaceAll("[^0-9]", "");
        return Integer.parseInt(num);
    }

    // JSONから値を抽出
    private static String extract(String json, String prefix, String suffix) {
        int s = json.indexOf(prefix);
        if (s == -1)
            return "?";
        s += prefix.length();
        int e = json.indexOf(suffix, s);
        if (e == -1)
            return json.substring(s);
        return json.substring(s, e);
    }

    // 英語種族値名→日本語
    private static String toJapanese(String stat) {
        switch (stat) {
            case "hp":
                return "HP";
            case "attack":
                return "攻撃";
            case "defense":
                return "防御";
            case "special-attack":
                return "特攻";
            case "special-defense":
                return "特防";
            case "speed":
                return "素早さ";
            default:
                return stat;
        }
    }

    // ポケモンIDから日本語名を取得（PokéAPIのspeciesエンドポイントを利用）
    private static String getJapaneseName(int pokeId) {
        try {
            String url = "https://pokeapi.co/api/v2/pokemon-species/" + pokeId;
            String json = fetch(url);
            // JSONから日本語名を抽出
            String nameJp = extract(json, "\"name\":\"", "\",\"language\":{\"name\":\"ja\"}");
            return nameJp.equals("?") ? "不明" : nameJp;
        } catch (Exception e) {
            return "不明";
        }
    }

    // クイズデータ用クラス
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
