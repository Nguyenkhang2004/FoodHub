import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class TestTime {
    public static void main(String[] args) {

//        ZoneId vietnamZoneId = ZoneId.of("Asia/Ho_Chi_Minh");
        Instant expiry = LocalDateTime.now().toInstant(ZoneOffset.UTC);
        System.out.println(expiry);
    }
}
