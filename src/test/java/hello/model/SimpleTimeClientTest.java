package hello.model;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import static org.junit.Assert.*;

public class SimpleTimeClientTest {

    @Test
    public void testDefaultConstructor() {
        SimpleTimeClient client = new SimpleTimeClient();
        assertNotNull(client.getLocalDateTime());
    }

    @Test
    public void testSetTime() {
        SimpleTimeClient client = new SimpleTimeClient();
        client.setTime(10, 30, 45);
        LocalDateTime dt = client.getLocalDateTime();
        assertEquals(10, dt.getHour());
        assertEquals(30, dt.getMinute());
        assertEquals(45, dt.getSecond());
    }

    @Test
    public void testSetDate() {
        SimpleTimeClient client = new SimpleTimeClient();
        client.setDate(2020, 6, 15);
        LocalDateTime dt = client.getLocalDateTime();
        assertEquals(2020, dt.getYear());
        assertEquals(6, dt.getMonthValue());
        assertEquals(15, dt.getDayOfMonth());
    }

    @Test
    public void testSetDateAndTime() {
        SimpleTimeClient client = new SimpleTimeClient();
        client.setDateAndTime(2021, 3, 25, 14, 30, 0);
        LocalDateTime dt = client.getLocalDateTime();
        assertEquals(2021, dt.getYear());
        assertEquals(3, dt.getMonthValue());
        assertEquals(25, dt.getDayOfMonth());
        assertEquals(14, dt.getHour());
        assertEquals(30, dt.getMinute());
        assertEquals(0, dt.getSecond());
    }

    @Test
    public void testToString() {
        SimpleTimeClient client = new SimpleTimeClient();
        client.setDateAndTime(2021, 3, 25, 14, 30, 0);
        String result = client.toString();
        assertTrue(result.contains("2021"));
    }

    @Test
    public void testGetZonedDateTime() {
        SimpleTimeClient client = new SimpleTimeClient();
        client.setDateAndTime(2021, 6, 15, 10, 0, 0);
        ZonedDateTime zonedDateTime = client.getZonedDateTime("America/New_York");
        assertNotNull(zonedDateTime);
        assertEquals("America/New_York", zonedDateTime.getZone().getId());
    }

    @Test
    public void testGetZonedDateTimeInvalidZone() {
        SimpleTimeClient client = new SimpleTimeClient();
        ZonedDateTime zonedDateTime = client.getZonedDateTime("Invalid/Zone");
        assertNotNull(zonedDateTime);
    }
}
