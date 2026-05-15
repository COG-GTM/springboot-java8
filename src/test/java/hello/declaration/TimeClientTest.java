package hello.declaration;

import hello.model.SimpleTimeClient;
import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.Assert.*;

public class TimeClientTest {

    @Test
    public void getZoneId_validZone_returnsCorrectZoneId() {
        ZoneId zoneId = TimeClient.getZoneId("America/New_York");
        assertEquals("America/New_York", zoneId.getId());
    }

    @Test
    public void getZoneId_invalidZone_fallsBackToSystemDefault() {
        ZoneId zoneId = TimeClient.getZoneId("Invalid/Zone");
        assertEquals(ZoneId.systemDefault(), zoneId);
    }

    @Test
    public void getZonedDateTime_viaSimpleTimeClient_returnsNonNull() {
        SimpleTimeClient client = new SimpleTimeClient();
        ZonedDateTime zdt = client.getZonedDateTime("Canada/Central");
        assertNotNull(zdt);
        assertEquals("Canada/Central", zdt.getZone().getId());
    }
}
