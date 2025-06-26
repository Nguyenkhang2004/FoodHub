import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class TestTime {
    public static void main(String[] args) {
      long TOKEN_EXPIRY_HOURS = 2;

        Instant expiryUtc = Instant.now().plus(TOKEN_EXPIRY_HOURS, ChronoUnit.HOURS);
        System.out.println(expiryUtc);
    }
}
