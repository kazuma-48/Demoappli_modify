import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TodayAnniversarySample {
    public static void main(String[] args){
        TodayAnniversary api = new TodayAnniversary();
        try {
            System.out.println("今日は" + LocalDate.now().format(DateTimeFormatter.ofPattern("M月d日、"))
                    + api.getTodayAnniversary() + "です。");
        } catch (IOException | java.net.URISyntaxException | org.json.JSONException e) {
            e.printStackTrace();
        }
    }    
}
