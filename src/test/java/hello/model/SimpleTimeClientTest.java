package hello.model;

import hello.declaration.TimeClient;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SimpleTimeClientTest {

    @Test
    public void setTimeUpdatesTimePart() {
        SimpleTimeClient client = new SimpleTimeClient();
        client.setTime(10, 20, 30);
        assertEquals(10, client.getLocalDateTime().getHour());
        assertEquals(20, client.getLocalDateTime().getMinute());
        assertEquals(30, client.getLocalDateTime().getSecond());
    }

    @Test
    public void setDateUpdatesDatePart() {
        SimpleTimeClient client = new SimpleTimeClient();
        client.setDate(2020, 5, 15);
        assertEquals(2020, client.getLocalDateTime().getYear());
        assertEquals(5, client.getLocalDateTime().getMonthValue());
        assertEquals(15, client.getLocalDateTime().getDayOfMonth());
    }

    @Test
    public void setDateAndTimeUpdatesBoth() {
        SimpleTimeClient client = new SimpleTimeClient();
        client.setDateAndTime(2021, 3, 10, 14, 30, 0);
        assertEquals(LocalDateTime.of(2021, 3, 10, 14, 30, 0), client.getLocalDateTime());
    }

    @Test
    public void getLocalDateTimeIsNotNull() {
        assertNotNull(new SimpleTimeClient().getLocalDateTime());
    }

    @Test
    public void getZonedDateTimeUsesProvidedZone() {
        TimeClient client = new SimpleTimeClient();
        ZonedDateTime zoned = client.getZonedDateTime("Canada/Central");
        assertNotNull(zoned);
        assertEquals("Canada/Central", zoned.getZone().getId());
    }
}
