    public List<String> getArtistSongs(String artist, int limit) throws Exception {
        String urlStr = "https://api.spotify.com/v1/search?q=" + URLEncoder.encode("artist:" + artist, "UTF-8") + "&type=track&limit=" + limit;
        URI uri = new URI(urlStr);
        URL url = uri.toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestPropery("Authorization", "Bearer " + accessToken);

        String response = readResponse(conn);
        JSONObject json = new JSONObject(response)
    }