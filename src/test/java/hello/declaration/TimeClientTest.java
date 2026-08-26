package hello.declaration;

import org.junit.Test;

import java.time.ZoneId;

import static org.junit.Assert.assertEquals;

public class TimeClientTest {

    @Test
    public void getsValidZoneId() {
        assertEquals(ZoneId.of("America/Los_Angeles"),
                TimeClient.getZoneId("America/Los_Angeles"));
    }

    @Test
    public void fallsBackToSystemZoneForInvalidZoneId() {
        assertEquals(ZoneId.systemDefault(), TimeClient.getZoneId("not-a-zone"));
    }
}
