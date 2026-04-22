package hello.declaration;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SimpleTimeClientTest {

    @Test
    void defaultConstructorInitializesLocalDateTime() {
        SimpleTimeClient client = new SimpleTimeClient();
        LocalDateTime dateTime = client.getLocalDateTime();
        assertNotNull(dateTime);
    }

    @Test
    void setTimeUpdatesOnlyTimeComponent() {
        SimpleTimeClient client = new SimpleTimeClient();
        client.setTime(10, 30, 45);

        LocalDateTime dateTime = client.getLocalDateTime();
        assertEquals(10, dateTime.getHour());
        assertEquals(30, dateTime.getMinute());
        assertEquals(45, dateTime.getSecond());
    }

    @Test
    void toStringReturnsIsoFormattedDateTime() {
        SimpleTimeClient client = new SimpleTimeClient();
        String result = client.toString();
        assertNotNull(result);
        assertEquals(client.getLocalDateTime().toString(), result);
    }

    @Test
    void defaultMethodGetZonedDateTimeReturnsZonedDateTime() {
        TimeClient client = new SimpleTimeClient();
        ZonedDateTime zoned = client.getZonedDateTime("America/New_York");

        assertNotNull(zoned);
        assertEquals(ZoneId.of("America/New_York"), zoned.getZone());
    }

    @Test
    void staticGetZoneIdReturnsSystemDefaultForInvalidZone() {
        ZoneId zoneId = TimeClient.getZoneId("Not/AValidZone");
        assertEquals(ZoneId.systemDefault(), zoneId);
    }

    @Test
    void staticGetZoneIdReturnsParsedZoneForValidZone() {
        ZoneId zoneId = TimeClient.getZoneId("UTC");
        assertEquals(ZoneId.of("UTC"), zoneId);
    }
}
