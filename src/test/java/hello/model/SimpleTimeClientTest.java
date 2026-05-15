package hello.model;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import static org.junit.Assert.*;

public class SimpleTimeClientTest {

    @Test
    public void defaultConstructor_setsDateAndTimeToApproximatelyNow() {
        SimpleTimeClient client = new SimpleTimeClient();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime clientTime = client.getLocalDateTime();

        assertEquals(now.getYear(), clientTime.getYear());
        assertEquals(now.getMonth(), clientTime.getMonth());
        assertEquals(now.getDayOfMonth(), clientTime.getDayOfMonth());
        assertEquals(now.getHour(), clientTime.getHour());
    }

    @Test
    public void setTime_updatesTimeComponent() {
        SimpleTimeClient client = new SimpleTimeClient();
        client.setTime(10, 30, 0);
        LocalDateTime result = client.getLocalDateTime();
        assertEquals(10, result.getHour());
        assertEquals(30, result.getMinute());
        assertEquals(0, result.getSecond());
    }

    @Test
    public void setDate_updatesDateComponent() {
        SimpleTimeClient client = new SimpleTimeClient();
        client.setDate(2020, 6, 15);
        LocalDateTime result = client.getLocalDateTime();
        assertEquals(2020, result.getYear());
        assertEquals(6, result.getMonthValue());
        assertEquals(15, result.getDayOfMonth());
    }

    @Test
    public void setDateAndTime_updatesBothComponents() {
        SimpleTimeClient client = new SimpleTimeClient();
        client.setDateAndTime(2021, 12, 25, 14, 30, 45);
        LocalDateTime result = client.getLocalDateTime();
        assertEquals(2021, result.getYear());
        assertEquals(12, result.getMonthValue());
        assertEquals(25, result.getDayOfMonth());
        assertEquals(14, result.getHour());
        assertEquals(30, result.getMinute());
        assertEquals(45, result.getSecond());
    }

    @Test
    public void toString_returnsNonNullString() {
        SimpleTimeClient client = new SimpleTimeClient();
        String result = client.toString();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void getZonedDateTime_returnsNonNull() {
        SimpleTimeClient client = new SimpleTimeClient();
        ZonedDateTime zdt = client.getZonedDateTime("America/New_York");
        assertNotNull(zdt);
        assertEquals("America/New_York", zdt.getZone().getId());
    }
}
