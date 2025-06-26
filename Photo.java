public class Photo {
    private final int id;
    private final String originalUrl;

    public Photo(int id, String originalUrl) {
        this.id = id;
        this.originalUrl = originalUrl;
    }

    public int getId() {
        return id;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }   
}
