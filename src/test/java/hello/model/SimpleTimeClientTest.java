package hello.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimpleTimeClientTest {

    @Test
    void setDateUsesDayMonthYearOrder() {
        SimpleTimeClient client = new SimpleTimeClient();
        client.setDate(25, 12, 2020);

        LocalDateTime result = client.getLocalDateTime();
        assertEquals(2020, result.getYear());
        assertEquals(12, result.getMonthValue());
        assertEquals(25, result.getDayOfMonth());
    }

    @Test
    void setDateAndTimeUsesDayMonthYearOrder() {
        SimpleTimeClient client = new SimpleTimeClient();
        client.setDateAndTime(31, 1, 2021, 13, 45, 7);

        assertEquals(LocalDateTime.of(2021, 1, 31, 13, 45, 7), client.getLocalDateTime());
    }
}
