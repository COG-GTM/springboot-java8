package hello.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimpleTimeClientTest {

    @Test
    void setDateUsesDayMonthYear() {
        SimpleTimeClient client = new SimpleTimeClient();
        client.setDate(15, 3, 2024);
        LocalDateTime result = client.getLocalDateTime();
        assertEquals(2024, result.getYear());
        assertEquals(3, result.getMonthValue());
        assertEquals(15, result.getDayOfMonth());
    }

    @Test
    void setDateAndTimeUsesDayMonthYear() {
        SimpleTimeClient client = new SimpleTimeClient();
        client.setDateAndTime(15, 3, 2024, 10, 20, 30);
        assertEquals(LocalDateTime.of(2024, 3, 15, 10, 20, 30), client.getLocalDateTime());
    }
}
