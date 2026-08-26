package hello.model;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.Assert.assertEquals;

public class SimpleTimeClientTest {

    @Test
    public void setTimeKeepsDateAndSetsTime() {
        SimpleTimeClient client = new SimpleTimeClient();
        LocalDate date = client.getLocalDateTime().toLocalDate();

        client.setTime(1, 2, 3);

        assertEquals(date, client.getLocalDateTime().toLocalDate());
        assertEquals(LocalTime.of(1, 2, 3), client.getLocalDateTime().toLocalTime());
    }

    @Test
    public void setDateAndTimeSetsBothValues() {
        SimpleTimeClient client = new SimpleTimeClient();

        client.setDateAndTime(2020, 5, 15, 10, 20, 30);

        assertEquals(LocalDateTime.of(2020, 5, 15, 10, 20, 30), client.getLocalDateTime());
    }

    @Test
    public void setDateKeepsCurrentTime() {
        SimpleTimeClient client = new SimpleTimeClient();
        LocalTime time = client.getLocalDateTime().toLocalTime();

        client.setDate(2020, 5, 15);

        assertEquals(LocalDate.of(2020, 5, 15), client.getLocalDateTime().toLocalDate());
        assertEquals(time, client.getLocalDateTime().toLocalTime());
    }

    @Test
    public void getsLocalDateTimeAndStringRepresentation() {
        SimpleTimeClient client = new SimpleTimeClient();

        assertEquals(client.getLocalDateTime().toString(), client.toString());
    }

    @Test
    public void getsZonedDateTimeForRequestedZone() {
        SimpleTimeClient client = new SimpleTimeClient();

        ZonedDateTime zoned = client.getZonedDateTime("America/Los_Angeles");

        assertEquals(ZoneId.of("America/Los_Angeles"), zoned.getZone());
        assertEquals(client.getLocalDateTime(), zoned.toLocalDateTime());
    }
}
