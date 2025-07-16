package QuizApp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.*;
import java.io.*;
import javax.net.ssl.HttpsURLConnection;

public class rogo extends JFrame {
    // サンプル都市リスト
    private static final String[] CITIES = {
            "Tokyo", "Paris", "New York", "London", "Sydney"
    };
    // 都市名とPlace IDのマップ（本来はAPIで取得）
    private static final Map<String, String> CITY_PLACE_IDS = new HashMap<>();
    static {
        CITY_PLACE_IDS.put("Tokyo", "ChIJ51cu8IcbXWARiRtXIothAS4");
        CITY_PLACE_IDS.put("Paris", "ChIJD7fiBh9u5kcRYJSMaMOCCwQ");
        CITY_PLACE_IDS.put("New York", "ChIJOwg_06VPwokRYv534QaPC8g");
        CITY_PLACE_IDS.put("London", "ChIJdd4hrwug2EcRmSrV3Vo6llI");
        CITY_PLACE_IDS.put("Sydney", "ChIJP3Sa8ziYEmsRUKgyFmh9AQM");
    }
    private String answerCity;
    private JLabel photoLabel;
    private JButton[] optionButtons = new JButton[4];
    private Random random = new Random();
    // Google Maps PlatformのAPIキー（実際は自分のAPIキーをセット）
    private static final String API_KEY = "YOUR_GOOGLE_MAPS_API_KEY";

    public rogo() {
        setTitle("都市写真クイズ（Google Places API）");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        photoLabel = new JLabel();
        photoLabel.setHorizontalAlignment(JLabel.CENTER);
        add(photoLabel, BorderLayout.CENTER);

        JPanel optionsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        for (int i = 0; i < 4; i++) {
            optionButtons[i] = new JButton();
            optionButtons[i].addActionListener(this::checkAnswer);
            optionsPanel.add(optionButtons[i]);
        }
        add(optionsPanel, BorderLayout.SOUTH);

        nextQuestion();
        setSize(500, 500);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void nextQuestion() {
        // 選択肢をランダムに4つ選ぶ
        List<String> cityList = new ArrayList<>(Arrays.asList(CITIES));
        Collections.shuffle(cityList);
        List<String> options = cityList.subList(0, 4);
        answerCity = options.get(random.nextInt(4));

        // Google Places APIで都市の写真を取得（サンプル: Place Photo APIのURLを生成）
        String placeId = CITY_PLACE_IDS.get(answerCity);
        String photoUrl = getPhotoUrlFromPlaceId(placeId);

        try {
            ImageIcon icon = new ImageIcon(new URL(photoUrl));
            Image img = icon.getImage().getScaledInstance(256, 256, Image.SCALE_SMOOTH);
            photoLabel.setIcon(new ImageIcon(img));
            photoLabel.setText("");
        } catch (Exception e) {
            photoLabel.setIcon(null);
            photoLabel.setText("写真の読み込み失敗");
        }

        Collections.shuffle(options);
        for (int i = 0; i < 4; i++) {
            optionButtons[i].setText(options.get(i));
            optionButtons[i].setActionCommand(options.get(i));
        }
    }

    // Place IDからPlace Photo APIのURLを生成（実際はAPIでphoto_referenceを取得する必要あり）
    private String getPhotoUrlFromPlaceId(String placeId) {
        // 本来はPlace Details APIでphoto_referenceを取得し、Place Photo APIで画像取得
        // ここではサンプルとしてGoogle Maps Static APIの都市画像を利用
        // 実運用ではphoto_referenceを取得して下記URLを生成してください
        // 例:
        // https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=xxxx&key=API_KEY
        return "https://maps.googleapis.com/maps/api/staticmap?center=" + placeId +
                "&zoom=12&size=400x400&key=" + API_KEY;
    }

    private void checkAnswer(ActionEvent e) {
        String selected = e.getActionCommand();
        if (selected.equals(answerCity)) {
            JOptionPane.showMessageDialog(this, "正解！");
        } else {
            JOptionPane.showMessageDialog(this, "不正解！正解は: " + answerCity);
        }
        nextQuestion();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(rogo::new);
    }
}