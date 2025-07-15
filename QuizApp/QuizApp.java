package QuizApp;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

import java.util.Scanner;

public class QuizApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("ポケモン図鑑番号クイズ！");
        System.out.print("ピカチュウの図鑑番号は？ > ");
        int answer = scanner.nextInt();

        if (answer == 25) {
            System.out.println("正解！");
            QuizApp app = new QuizApp();
            app.play(); // 正解音を再生
        } else {
            System.out.println("不正解。正解は 25 です。");
        }

        scanner.close();
    }

    // 正解音を再生するメソッド
    public void play() {
        try {
            // 音声ファイルのパスを明示的に指定（プロジェクト直下の場合）
            File soundFile = new File("c:\\Users\\243102\\Desktop\\Demoappli_modify\\QuizApp\\correct.wav");
            if (!soundFile.exists()) {
                System.out.println("音声ファイルが見つかりません: " + soundFile.getAbsolutePath());
                return;
            }
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
            // 再生が終わるまで待機
            clip.drain();
            clip.close();
            audioStream.close();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("音声ファイルの再生に失敗しました: " + e.getMessage());
        }
    }
}