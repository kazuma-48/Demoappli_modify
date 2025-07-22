package QuizApp;

import java.util.Scanner;

public class QuizMain {
        public static void main(String[] args) {
                Scanner scanner = new Scanner(System.in);
                while (true) {
                        System.out.println("\n==== クイズアプリ ====");
                        System.out.println("1. 歴史クイズ");
                        System.out.println("2. 英語クイズ");
                        System.out.println("3. 漢字クイズ");
                        System.out.println("4. 音楽クイズ");
                        System.out.println("5. 芸術クイズ");
                        System.out.println("0. 終了");
                        System.out.print("ジャンルを選んでください: ");
                        String input = scanner.nextLine();
                        if (input.equals("0")) {
                                System.out.println("終了します。");
                                break;
                        } else if (input.equals("1")) {
                                runNihonshiQuiz(scanner);
                        } else if (input.equals("2")) {
                                runEnglishQuiz(scanner);
                        } else if (input.equals("3")) {
                                runKanjiQuiz(scanner);
                        } else if (input.equals("4")) {
                                runMusicQuiz(scanner);
                        } else if (input.equals("5")) {
                                runArtQuiz(scanner);
                        } else {
                                System.out.println("無効な選択です。");
                        }
                }
                scanner.close();
        }

        private static void runNihonshiQuiz(Scanner scanner) {
                while (true) {
                        History.Quiz quiz = History.getQuiz();
                        System.out.println("\n--- 歴史クイズ ---");
                        System.out.println(quiz.question);
                        for (int i = 0; i < quiz.choices.length; i++) {
                                System.out.printf("%d. %s\n", i + 1, quiz.choices[i]);
                        }
                        System.out.print("番号で回答してください（0でジャンル選択に戻る）: ");
                        String ans = scanner.nextLine();
                        if (ans.equals("0"))
                                break;
                        int ansIdx = -1;
                        try {
                                ansIdx = Integer.parseInt(ans) - 1;
                        } catch (Exception e) {
                        }
                        if (ansIdx >= 0 && ansIdx < quiz.choices.length) {
                                if (ansIdx == quiz.correctIdx) {
                                        System.out.println("\u001b[32m【正解！】\u001b[0m");
                                } else {
                                        System.out.println("\u001b[31m【不正解】\u001b[0m 正解: "
                                                        + quiz.choices[quiz.correctIdx]);
                                }
                        } else {
                                System.out.println("無効な入力です。");
                        }
                        System.out.println("次の問題へ進みます。\n");
                }
        }

        private static void runEnglishQuiz(Scanner scanner) {
                while (true) {
                        English.Quiz quiz = English.getQuiz();
                        System.out.println("\n--- 英語クイズ ---");
                        System.out.println(quiz.question);
                        for (int i = 0; i < quiz.choices.length; i++) {
                                System.out.printf("%d. %s\n", i + 1, quiz.choices[i]);
                        }
                        System.out.print("番号で回答してください（0でジャンル選択に戻る）: ");
                        String ans = scanner.nextLine();
                        if (ans.equals("0"))
                                break;
                        int ansIdx = -1;
                        try {
                                ansIdx = Integer.parseInt(ans) - 1;
                        } catch (Exception e) {
                        }
                        if (ansIdx >= 0 && ansIdx < quiz.choices.length) {
                                if (ansIdx == quiz.correctIdx) {
                                        System.out.println("\u001b[32m【正解！】\u001b[0m");
                                } else {
                                        System.out.println("\u001b[31m【不正解】\u001b[0m 正解: "
                                                        + quiz.choices[quiz.correctIdx]);
                                }
                        } else {
                                System.out.println("無効な入力です。");
                        }
                        System.out.println("次の問題へ進みます。\n");
                }
        }

        private static void runKanjiQuiz(Scanner scanner) {
                while (true) {
                        Kanji.Quiz quiz = Kanji.getQuiz();
                        System.out.println("\n--- 漢字クイズ ---");
                        System.out.println(quiz.question);
                        for (int i = 0; i < quiz.choices.length; i++) {
                                System.out.printf("%d. %s\n", i + 1, quiz.choices[i]);
                        }
                        System.out.print("番号で回答してください（0でジャンル選択に戻る）: ");
                        String ans = scanner.nextLine();
                        if (ans.equals("0"))
                                break;
                        int ansIdx = -1;
                        try {
                                ansIdx = Integer.parseInt(ans) - 1;
                        } catch (Exception e) {
                        }
                        if (ansIdx >= 0 && ansIdx < quiz.choices.length) {
                                if (ansIdx == quiz.correctIdx) {
                                        System.out.println("\u001b[32m【正解！】\u001b[0m");
                                } else {
                                        System.out.println("\u001b[31m【不正解】\u001b[0m 正解: "
                                                        + quiz.choices[quiz.correctIdx]);
                                }
                        } else {
                                System.out.println("無効な入力です。");
                        }
                        System.out.println("次の問題へ進みます。\n");
                }
        }

        private static void runMusicQuiz(Scanner scanner) {
                while (true) {
                        try {
                                Music.Quiz quiz = Music.getQuiz();
                                System.out.println("\n--- 音楽クイズ ---");
                                System.out.println(quiz.question);
                                for (int i = 0; i < quiz.choices.length; i++) {
                                        System.out.printf("%d. %s\n", i + 1, quiz.choices[i]);
                                }
                                System.out.print("番号で回答してください（0でジャンル選択に戻る）: ");
                                String ans = scanner.nextLine();
                                if (ans.equals("0"))
                                        break;
                                int ansIdx = -1;
                                try {
                                        ansIdx = Integer.parseInt(ans) - 1;
                                } catch (Exception e) {
                                }
                                if (ansIdx >= 0 && ansIdx < quiz.choices.length) {
                                        if (ansIdx == quiz.correctIdx) {
                                                System.out.println("\u001b[32m【正解！】\u001b[0m");
                                        } else {
                                                System.out.println("\u001b[31m【不正解】\u001b[0m 正解: "
                                                                + quiz.choices[quiz.correctIdx]);
                                        }
                                } else {
                                        System.out.println("無効な入力です。");
                                }
                                System.out.println("次の問題へ進みます。\n");
                        } catch (Exception e) {
                                System.out.println("APIエラー: " + e.getMessage());
                                break;
                        }
                }
        }
        
        private static void runArtQuiz(Scanner scanner) {
                while (true) {
                        Art.Quiz quiz = Art.getQuiz();
                        System.out.println("\n--- 芸術クイズ ---");
                        System.out.println(quiz.question);
                        for (int i = 0; i < quiz.choices.length; i++) {
                                System.out.printf("%d. %s\n", i + 1, quiz.choices[i]);
                        }
                        System.out.print("番号で回答してください（0でジャンル選択に戻る）: ");
                        String ans = scanner.nextLine();
                        if (ans.equals("0"))
                                break;
                        int ansIdx = -1;
                        try {
                                ansIdx = Integer.parseInt(ans) - 1;
                        } catch (Exception e) {
                        }
                        if (ansIdx >= 0 && ansIdx < quiz.choices.length) {
                                if (ansIdx == quiz.correctIdx) {
                                        System.out.println("\u001b[32m【正解！】\u001b[0m");
                                } else {
                                        System.out.println("\u001b[31m【不正解】\u001b[0m 正解: "
                                                        + quiz.choices[quiz.correctIdx]);
                                }
                        } else {
                                System.out.println("無効な入力です。");
                        }
                        System.out.println("次の問題へ進みます。\n");
                }
        }
}