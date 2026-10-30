package hello.declaration;

import hello.model.SimpleTimeClient;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TimeClientTest {

    @Test
    public void resolvesValidZoneId() {
        assertEquals(ZoneId.of("Canada/Central"), TimeClient.getZoneId("Canada/Central"));
    }

    @Test
    public void fallsBackToSystemZoneForInvalidId() {
        assertEquals(ZoneId.systemDefault(), TimeClient.getZoneId("Not/AZone"));
    }

    @Test
    public void zonedDateTimeKeepsLocalDateTime() {
        SimpleTimeClient client = new SimpleTimeClient();
        client.setTime(10, 30, 0);
        ZonedDateTime zoned = client.getZonedDateTime("Canada/Central");
        assertEquals(client.getLocalDateTime(), zoned.toLocalDateTime());
        assertEquals(ZoneId.of("Canada/Central"), zoned.getZone());
    }
}
