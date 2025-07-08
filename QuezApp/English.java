package QuezApp;

public class English {
    // CLI用：問題文・選択肢・正解インデックスを返す
    public static Quiz getQuiz() {
        // 英和辞書ファイルやAPIを使わず、JavaのRandomで英単語を生成（例: 5文字のランダム英単語）
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        java.util.Random rand = new java.util.Random();
        String answer = "";
        for (int i = 0; i < 5; i++)
            answer += alphabet.charAt(rand.nextInt(alphabet.length()));
        String question = "次の意味に当てはまる英単語の日本語訳はどれ？\n" + answer + " の意味";
        // 正解の日本語訳もダミーで生成
        String answerJa = answer + "の訳";
        String[] choices = new String[4];
        java.util.List<String> choiceList = new java.util.ArrayList<>();
        choiceList.add(answerJa);
        // ダミー日本語訳を3つ生成
        for (int i = 0; i < 3; i++) {
            String dummy = "ダミー訳" + (i + 1);
            choiceList.add(dummy);
        }
        java.util.Collections.shuffle(choiceList);
        int correctIdx = 0;
        for (int i = 0; i < 4; i++) {
            choices[i] = choiceList.get(i);
            if (choices[i].equals(answerJa))
                correctIdx = i;
        }
        return new Quiz(question, choices, correctIdx);
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
