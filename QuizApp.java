import javax.swing.*;
import java.awt.*;

public class QuizApp {
    public static void main(String[] args) {
        JFrame frame = new JFrame("HTML画面表示サンプル");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 700);

        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family:sans-serif; text-align:center;'>");
        html.append("<h1>ボタン遷移サンプル</h1>");
        html.append("<table style='margin:auto; border-spacing:16px;'>");
        int btnNum = 1;
        for (int i = 0; i < 4; i++) {
            html.append("<tr><button onclick id = 'jannru" + i + "'>ジャンル " + i + "</button></tr>");
            for (int j = 0; j < 4; j++) {
                html.append("<td><button onclick id = 'line" + btnNum + "'>ジャンル" + btnNum + "</button></td>");
                btnNum++;
            }
        }
        JEditorPane editorPane = new JEditorPane("text/html", html.toString());
        editorPane.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(editorPane);

        frame.add(scrollPane);
        frame.setVisible(true);
    }
}