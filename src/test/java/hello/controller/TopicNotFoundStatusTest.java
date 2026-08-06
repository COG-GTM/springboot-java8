package hello.controller;

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
 * <p>{@code TopicService.getTopicWithId} returns an empty {@link java.util.Optional} on a miss, which
 * the controller translates into a 404.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TopicNotFoundStatusTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void unknownTopicIdReturnsNotFound() {
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
