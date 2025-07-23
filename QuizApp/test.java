package QuizApp;

public class test {
    public static void main(String[] args) {
        try {
            // Art Institute of Chicago API から作品リストを取得
            String apiUrl = "https://api.artic.edu/api/v1/artworks?page=1&limit=100";
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) java.net.URI.create(apiUrl).toURL()
                    .openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            java.util.Set<String> artistNames = new java.util.LinkedHashSet<>();
            try (java.io.BufferedReader br = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                org.json.JSONObject jsonResponse = new org.json.JSONObject(response.toString());
                org.json.JSONArray data = jsonResponse.optJSONArray("data");
                if (data != null && data.length() > 0) {
                    for (int i = 0; i < data.length(); i++) {
                        org.json.JSONObject artwork = data.getJSONObject(i);
                        String artist = artwork.optString("artist_title", null);
                        if (artist != null && !artist.isEmpty()) {
                            artistNames.add(artist);
                        }
                    }
                }
            }
            System.out.println("AIC APIから取得できた作者名一覧:");
            for (String name : artistNames) {
                System.out.println("- " + name);
            }
            if (artistNames.isEmpty()) {
                System.out.println("作者名が取得できませんでした。");
            }
        } catch (Exception e) {
            System.err.println("エラー: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
