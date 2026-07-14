package hello.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimpleTimeClientTest {

    @Test
    void setDateAndTimeSetsFullTimestamp() {
        SimpleTimeClient client = new SimpleTimeClient();
        client.setDateAndTime(2021, 6, 20, 10, 30, 45);
        assertEquals(LocalDateTime.of(2021, 6, 20, 10, 30, 45), client.getLocalDateTime());
    }

    @Test
    void toStringMatchesLocalDateTime() {
        SimpleTimeClient client = new SimpleTimeClient();
        client.setDateAndTime(2021, 6, 20, 10, 30, 45);
        assertEquals(client.getLocalDateTime().toString(), client.toString());
    }

    @Test
    void setTimeKeepsDateAndUpdatesTime() {
        SimpleTimeClient client = new SimpleTimeClient();
        client.setDateAndTime(2021, 6, 20, 10, 30, 45);
        client.setTime(1, 2, 3);
        assertEquals(LocalDateTime.of(2021, 6, 20, 1, 2, 3), client.getLocalDateTime());
    }

    @Test
    void setDateKeepsTimeAndUpdatesDate() {
        SimpleTimeClient client = new SimpleTimeClient();
        client.setDateAndTime(2021, 6, 20, 10, 30, 45);
        client.setDate(2000, 1, 1);
        assertEquals(LocalDateTime.of(2000, 1, 1, 10, 30, 45), client.getLocalDateTime());
    }
}
