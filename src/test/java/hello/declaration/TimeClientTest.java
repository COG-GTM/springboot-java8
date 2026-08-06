package hello.declaration;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

import hello.model.SimpleTimeClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers the {@code static} and {@code default} methods on the interface and the
 * {@link SimpleTimeClient} implementation of them.
 */
class TimeClientTest {

    @Test
    void getZoneIdResolvesAKnownZone() {
        assertThat(TimeClient.getZoneId("Canada/Central")).isEqualTo(ZoneId.of("Canada/Central"));
    }

    @Test
    void getZoneIdFallsBackToTheSystemZoneForAnInvalidZone() {
        assertThat(TimeClient.getZoneId("Not/A/Zone")).isEqualTo(ZoneId.systemDefault());
        assertThat(TimeClient.getZoneId("")).isEqualTo(ZoneId.systemDefault());
    }

    @Test
    void getZonedDateTimeCombinesTheLocalDateTimeWithTheRequestedZone() {
        SimpleTimeClient client = new SimpleTimeClient();
        client.setDateAndTime(2020, 5, 17, 13, 45, 30);

        ZonedDateTime zoned = client.getZonedDateTime("Canada/Central");

        assertThat(zoned.toLocalDateTime()).isEqualTo(LocalDateTime.of(2020, 5, 17, 13, 45, 30));
        assertThat(zoned.getZone()).isEqualTo(ZoneId.of("Canada/Central"));
    }

    @Test
    void getZonedDateTimeUsesTheSystemZoneWhenTheZoneIsInvalid() {
        SimpleTimeClient client = new SimpleTimeClient();

        assertThat(client.getZonedDateTime("Middle/Earth").getZone()).isEqualTo(ZoneId.systemDefault());
    }

    @Test
    void newClientIsInitialisedWithTheCurrentDateTime() {
        LocalDateTime before = LocalDateTime.now().minusMinutes(1);

        SimpleTimeClient client = new SimpleTimeClient();

        assertThat(client.getLocalDateTime()).isAfter(before);
        assertThat(client.toString()).isEqualTo(client.getLocalDateTime().toString());
    }

    @Test
    void setTimeKeepsTheDateAndReplacesTheTime() {
        SimpleTimeClient client = new SimpleTimeClient();
        client.setDateAndTime(2020, 5, 17, 1, 2, 3);

        client.setTime(23, 59, 58);

        assertThat(client.getLocalDateTime()).isEqualTo(LocalDateTime.of(2020, 5, 17, 23, 59, 58));
    }

    @Test
    void setDateKeepsTheTimeAndReplacesTheDate() {
        SimpleTimeClient client = new SimpleTimeClient();
        client.setDateAndTime(2020, 5, 17, 8, 30, 0);

        // setDate/setDateAndTime declare their parameters as (day, month, year) but delegate to
        // LocalDate.of(..) positionally, so the first argument is really the year. Asserted as
        // implemented rather than as named.
        client.setDate(2021, 12, 25);

        assertThat(client.getLocalDateTime()).isEqualTo(LocalDateTime.of(2021, 12, 25, 8, 30, 0));
    }
}
