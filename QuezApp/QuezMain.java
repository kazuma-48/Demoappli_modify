package QuezApp;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class QuezMain {
        public static void main(String[] args) throws IOException {
                StringBuilder html = new StringBuilder();
                html.append(
                                "<html>\n<head>\n<meta charset='UTF-8'>\n<title>クイズアプリ</title>\n<style>\n.question-box {\n  background: #e3f2fd;\n  border: 3px solid #1976d2;\n  border-radius: 24px;\n  padding: 40px 30px;\n  margin: 40px auto 30px auto;\n  max-width: 700px;\n  font-size: 2em;\n  text-align: center;\n  box-shadow: 0 4px 16px rgba(25,118,210,0.08);\n}\n.home-btn {\n  margin-top: 30px;\n  font-size: 1.2em;\n  padding: 10px 40px;\n  border-radius: 8px;\n  border: none;\n  background: #1976d2;\n  color: #fff;\n  cursor: pointer;\n}\n.home-btn:hover { background: #0d47a1; }\n</style>\n<script>\nfunction goToNext(id) {\n  document.getElementById('main').style.display = 'none';\n  document.getElementById(id).style.display = 'block';\n}\nfunction goBack(id) {\n  document.getElementById(id).style.display = 'none';\n  document.getElementById('main').style.display = 'block';\n}\nfunction reloadQuiz() {\n  location.reload();\n}\nfunction disableChoices() {\n  var btns = document.querySelectorAll('.choice-btn');\n  btns.forEach(function(btn){ btn.disabled = true; });\n}\nfunction showHomeFromQuiz() {\n  document.getElementById('historyScreen').style.display = 'none';\n  document.getElementById('main').style.display = 'block';\n}\n</script>\n</head>\n<body>");
                html.append("<div id='main'>\n<table style='margin:auto;'><tr>");
                html.append("<td><button onclick=\"goToNext('historyGenreScreen')\" style='font-size: 100px;'>歴史</button></td>");
                html.append("<td><button onclick=\"goToNext('geoScreen')\" style='font-size: 100px;'>地理</button></td>");
                html.append("<td><button onclick=\"goToNext('englishScreen')\" style='font-size:100px; margin:30px;'>英語</button></td>");
                html.append("</tr></table>\n</div>");
                // 歴史ジャンル選択画面
                html.append("<div id='historyGenreScreen' style='display:none; text-align:center;'>");
                html.append("<h2>歴史ジャンルを選択</h2>");
                html.append("<button onclick=\"goToNext('nihonshiScreen')\" style='font-size:60px; margin:30px;'>日本史</button>");
                html.append("<button onclick=\"goToNext('worldshiScreen')\" style='font-size:60px; margin:30px;'>世界史</button>");

                html.append("<br><button class='home-btn' onclick=\"goBack('historyGenreScreen')\">ホームに戻る</button>");
                html.append("</div>");
                // 日本史クイズ画面
                html.append("<div id='nihonshiScreen' style='display:none;'>");
                String quizHtml = JapaneseHistory.getQuizHtml();
                quizHtml = quizHtml.replaceAll("<button onclick=\\\"checkHistoryAnswer",
                                "<button class='choice-btn' onclick=\\\"disableChoices();checkHistoryAnswer");
                html.append(quizHtml);
                html.append("</div>");
                // 世界史クイズ画面（ダミー）
                html.append("<div id='worldshiScreen' style='display:none;'><h2>世界史クイズは準備中です</h2><button class='home-btn' onclick=\"goBack('worldshiScreen')\">ジャンル選択に戻る</button></div>");
                // 地理画面のダミー
                html.append("<div id='geoScreen' style='display:none;'><h2>地理クイズは準備中です</h2><button onclick=\"goBack('geoScreen')\">ホームに戻る</button></div>");
                // 英語クイズ画面
                html.append("<div id='englishScreen' style='display:none;'>");
                String englishQuizHtml = English.getQuizHtml();
                html.append(englishQuizHtml);
                html.append("</div>");
                // 日本史ジャンルから問題画面に遷移するJS
                html.append("<script>\n" +
                                "function goToNext(id) {\n" +
                                "  document.getElementById('main').style.display = 'none';\n" +
                                "  document.getElementById(id).style.display = 'block';\n" +
                                "}\n" +
                                "function goBack(id) {\n" +
                                "  document.getElementById(id).style.display = 'none';\n" +
                                "  document.getElementById('main').style.display = 'block';\n" +
                                "}\n" +
                                "</script>");
                html.append("</body></html>");

                File htmlFile = new File("test.html");
                try (FileWriter writer = new FileWriter(htmlFile)) {
                        writer.write(html.toString());
                }

                Desktop.getDesktop().browse(htmlFile.toURI());
        }
}