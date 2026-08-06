package hello.controller;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Requesting an unknown topic id is the one place where the HTTP status carries real information, and
 * MockMvc rethrows unhandled exceptions instead of running the error dispatch, so this one case is
 * driven over a real connection.
 *
 * <p>{@code TopicService.getTopicWithId} calls {@code Optional.get()} on a miss, which throws
 * {@link java.util.NoSuchElementException} and surfaces as HTTP 500. That is asserted here as the
 * <em>current</em> behaviour; the disabled test below is the replacement for when a parallel change
 * turns the miss into a proper 404.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TopicNotFoundStatusTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void unknownTopicIdCurrentlyReturnsInternalServerError() {
        ResponseEntity<String> response = restTemplate.getForEntity("/topic/does-not-exist", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @Disabled("Enable, and delete unknownTopicIdCurrentlyReturnsInternalServerError, once an unknown id is a 404")
    void unknownTopicIdShouldReturnNotFound() {
        ResponseEntity<String> response = restTemplate.getForEntity("/topic/does-not-exist", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void knownTopicIdReturnsOkWithTheUnchangedJsonShape() {
        ResponseEntity<String> response = restTemplate.getForEntity("/topic/spring", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .contains("\"id\":\"spring\"")
                .contains("\"subjectName\":\"Spring Framework\"")
                .contains("\"subjectDescription\":\"Spring Framework Description\"");
    }
}
