import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class QuezApp {
    public static void main(String[] args) throws IOException {
        StringBuilder html = new StringBuilder();
        html.append(
                "<html>\n<head>\n<meta charset='UTF-8'>\n<title>クイズアプリ</title>\n<style>\n.question-box {\n  background: #e3f2fd;\n  border: 3px solid #1976d2;\n  border-radius: 24px;\n  padding: 40px 30px;\n  margin: 40px auto 30px auto;\n  max-width: 700px;\n  font-size: 2em;\n  text-align: center;\n  box-shadow: 0 4px 16px rgba(25,118,210,0.08);\n}\n.home-btn {\n  margin-top: 30px;\n  font-size: 1.2em;\n  padding: 10px 40px;\n  border-radius: 8px;\n  border: none;\n  background: #1976d2;\n  color: #fff;\n  cursor: pointer;\n}\n.home-btn:hover { background: #0d47a1; }\n</style>\n<script>\nfunction goToNext(id) {\n  document.getElementById('main').style.display = 'none';\n  document.getElementById(id).style.display = 'block';\n}\nfunction goBack(id) {\n  document.getElementById(id).style.display = 'none';\n  document.getElementById('main').style.display = 'block';\n}\nfunction reloadQuiz() {\n  location.reload();\n}\nfunction disableChoices() {\n  var btns = document.querySelectorAll('.choice-btn');\n  btns.forEach(function(btn){ btn.disabled = true; });\n}\nfunction showHomeFromQuiz() {\n  document.getElementById('historyScreen').style.display = 'none';\n  document.getElementById('main').style.display = 'block';\n}\n</script>\n</head>\n<body>");
        html.append("<div id='main'>\n<table style='margin:auto;'><tr>");
        html.append("<td><button onclick=\"goToNext('historyScreen')\" style='font-size: 100px;'>歴史</button></td>");
        html.append("<td><button onclick=\"goToNext('geoScreen')\" style='font-size: 100px;'>地理</button></td>");
        html.append("</tr></table>\n</div>");
        // 歴史クイズ画面
        html.append("<div id='historyScreen' style='display:none;'>");
        // クイズ本体
        String quizHtml = history.getQuizHtml();
        // "説明："→"問題："に置換し、問題文を図形（.question-box）で囲む
        quizHtml = quizHtml.replace("説明：", "問題：");
        quizHtml = quizHtml.replace("<p>問題：", "<div class='question-box'>問題：");
        quizHtml = quizHtml.replace("</p>", "</div>");
        // 選択肢ボタンにクラス付与
        quizHtml = quizHtml.replaceAll("<button onclick=\\\"checkHistoryAnswer",
                "<button class='choice-btn' onclick=\\\"disableChoices();checkHistoryAnswer");
        // 戻るボタンを消し、「次の問題へ」ボタンに置換
        quizHtml = quizHtml.replace("<button onclick=\\\"goBack()\\\" style='margin-top:30px;'>戻る</button>",
                "<button onclick=\\\"reloadQuiz()\\\" style='margin-top:30px;'>次の問題へ</button>");
        html.append(quizHtml);
        html.append("</div>");
        // 地理画面のダミー
        html.append(
                "<div id='geoScreen' style='display:none;'><h2>地理クイズは準備中です</h2><button onclick=\"goBack('geoScreen')\">ホームに戻る</button></div>");
        html.append("</body></html>");

        File htmlFile = new File("test.html");
        try (FileWriter writer = new FileWriter(htmlFile)) {
            writer.write(html.toString());
        }

        Desktop.getDesktop().browse(htmlFile.toURI());
    }
}