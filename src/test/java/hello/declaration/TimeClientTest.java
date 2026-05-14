package hello.declaration;

import org.junit.Test;

import java.time.ZoneId;

import static org.junit.Assert.*;

public class TimeClientTest {

    @Test
    public void testGetZoneIdValid() {
        ZoneId zone = TimeClient.getZoneId("America/New_York");
        assertEquals("America/New_York", zone.getId());
    }

    @Test
    public void testGetZoneIdInvalid() {
        ZoneId zone = TimeClient.getZoneId("Invalid/Zone");
        assertEquals(ZoneId.systemDefault(), zone);
    }

    @Test
    public void testGetZoneIdUTC() {
        ZoneId zone = TimeClient.getZoneId("UTC");
        assertEquals("UTC", zone.getId());
    }
}
