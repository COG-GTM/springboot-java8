package hello.model;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import org.junit.Test;

public class SimpleTimeClientTest {

    @Test
    public void testDefaultConstructor() {
        SimpleTimeClient client = new SimpleTimeClient();
        assertNotNull(client.getLocalDateTime());
    }

    @Test
    public void testSetTime() {
        SimpleTimeClient client = new SimpleTimeClient();
        client.setTime(14, 30, 0);
        LocalDateTime dt = client.getLocalDateTime();
        assertEquals(14, dt.getHour());
        assertEquals(30, dt.getMinute());
        assertEquals(0, dt.getSecond());
    }

    @Test
    public void testSetDate() {
        SimpleTimeClient client = new SimpleTimeClient();
        client.setDate(2022, 6, 15);
        LocalDateTime dt = client.getLocalDateTime();
        assertEquals(2022, dt.getYear());
        assertEquals(6, dt.getMonthValue());
        assertEquals(15, dt.getDayOfMonth());
    }

    @Test
    public void testSetDateAndTime() {
        SimpleTimeClient client = new SimpleTimeClient();
        client.setDateAndTime(2022, 6, 15, 10, 30, 45);
        LocalDateTime dt = client.getLocalDateTime();
        assertEquals(2022, dt.getYear());
        assertEquals(6, dt.getMonthValue());
        assertEquals(15, dt.getDayOfMonth());
        assertEquals(10, dt.getHour());
        assertEquals(30, dt.getMinute());
        assertEquals(45, dt.getSecond());
    }

    @Test
    public void testToString() {
        SimpleTimeClient client = new SimpleTimeClient();
        assertNotNull(client.toString());
    }

    @Test
    public void testGetZonedDateTime() {
        SimpleTimeClient client = new SimpleTimeClient();
        ZonedDateTime zonedDateTime = client.getZonedDateTime("Canada/Central");
        assertNotNull(zonedDateTime);
    }
}
