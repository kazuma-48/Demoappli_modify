import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class QuezApp {
    public static void main(String[] args) throws IOException {
        String html = """
                <html>
                <head>
                    <meta charset = 'UTF-8'>
                    <title>クイズアプリ</title>
                    <script>
                        function goToNext() {
                            document.getElementById('main').style.display = 'none';
                            document.getElementById('next').style.display = 'block';
                        }
                        function goBack() {
                            document.getElementById('main').style.display = 'block';
                            document.getElementById('next').style.display = 'none';
                        }
                    </script>
                </head>
                <body>
                    <div id='main'>
                        <table style='margin:auto;'>
                            <tr>
                                <td><button onclick='goToNext()' style='font-size: 100px;'>歴史</button></td>
                                <td><button onclick='goToNext()' style='font-size: 100px;'>地理</button></td>
                                <td><button onclick='goToNext()' style='font-size: 100px;'>雑学</button></td>
                            </tr>
                            <tr>
                                <td><button onclick='goToNext()' style='font-size: 100px;'></button></td>
                                <td><button onclick='goToNext()' style='font-size: 100px;'>文学</button></td>
                                <td><button onclick='goToNext()' style='font-size: 100px;'>芸術</button></td>
                            </tr>
                        </table>
                    </div>
                    <div id='next' style='display:none;'>
                        <h2>これは2つ目の画面です</h2>
                        <button onclick='goBack()'>戻る</button>
                    </div>
                </body>
                </html>
                """;

        File htmlFile = new File("test.html");
        try (FileWriter writer = new FileWriter(htmlFile)) {
            writer.write(html);
        }

        Desktop.getDesktop().browse(htmlFile.toURI());
    }
}