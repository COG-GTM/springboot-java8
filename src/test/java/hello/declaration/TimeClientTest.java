package hello.declaration;

import static org.junit.Assert.*;

import java.time.ZoneId;
import org.junit.Test;

public class TimeClientTest {

    @Test
    public void testGetZoneIdValid() {
        ZoneId zoneId = TimeClient.getZoneId("America/New_York");
        assertNotNull(zoneId);
        assertEquals("America/New_York", zoneId.getId());
    }

    @Test
    public void testGetZoneIdInvalid() {
        ZoneId zoneId = TimeClient.getZoneId("Invalid/Zone");
        assertNotNull(zoneId);
        assertEquals(ZoneId.systemDefault(), zoneId);
    }
}
