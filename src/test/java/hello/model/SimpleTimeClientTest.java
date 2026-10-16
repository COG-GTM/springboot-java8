package hello.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimpleTimeClientTest {

    @Test
    void setDateUsesDayMonthYearOrder() {
        SimpleTimeClient client = new SimpleTimeClient();
        client.setDate(25, 12, 2024);
        assertEquals(LocalDate.of(2024, 12, 25), client.getLocalDateTime().toLocalDate());
    }

    @Test
    void setDateAndTimeUsesDayMonthYearOrder() {
        SimpleTimeClient client = new SimpleTimeClient();
        client.setDateAndTime(31, 1, 2025, 13, 45, 30);
        assertEquals(LocalDateTime.of(2025, 1, 31, 13, 45, 30), client.getLocalDateTime());
    }
}
