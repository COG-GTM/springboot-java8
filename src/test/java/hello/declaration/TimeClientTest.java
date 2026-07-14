package hello.declaration;

import hello.model.SimpleTimeClient;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeClientTest {

    @Test
    void getZoneIdReturnsRequestedZoneWhenValid() {
        assertEquals(ZoneId.of("Europe/London"), TimeClient.getZoneId("Europe/London"));
    }

    @Test
    void getZoneIdFallsBackToSystemDefaultWhenInvalid() {
        assertEquals(ZoneId.systemDefault(), TimeClient.getZoneId("Not/AZone"));
    }

    @Test
    void getZonedDateTimeUsesLocalDateTimeAndZone() {
        SimpleTimeClient client = new SimpleTimeClient();
        client.setDateAndTime(2021, 6, 20, 10, 30, 45);

        ZonedDateTime zoned = client.getZonedDateTime("America/New_York");

        assertEquals(client.getLocalDateTime(), zoned.toLocalDateTime());
        assertEquals(ZoneId.of("America/New_York"), zoned.getZone());
    }
}
