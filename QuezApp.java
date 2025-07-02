import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class QuezApp {
    public static void main(String[] args) throws IOException {
        String html = """
                <html>
                <head>
                    <meta charset = "UTF-8">
                    <title>クイズアプリ</title>
                </head>
                <body>
                    <button onclick="alert('クリックされました！')">クリック</button>
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