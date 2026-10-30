package hello;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Characterization tests: expected bodies are the responses captured from the original
 * Java 8 / Spring Boot 2.0.2 build, so any behaviour change during the upgrade fails here.
 * The context is reset after each test because TopicService and the greeting counter are stateful.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(QuoteServiceStub.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class EndpointParityTest {

    private static final String SPRING = "{\"id\":\"spring\",\"subjectName\":\"Spring Framework\",\"subjectDescription\":\"Spring Framework Description\"}";
    private static final String JAVA = "{\"id\":\"java\",\"subjectName\":\"Core Java\",\"subjectDescription\":\"Java Description\"}";
    private static final String JAVASCRIPT = "{\"id\":\"javascript\",\"subjectName\":\"javascript Framework\",\"subjectDescription\":\"javascript Framework Description\"}";
    private static final String KOTLIN = "{\"id\":\"kotlin\",\"subjectName\":\"Kotlin\",\"subjectDescription\":\"Kotlin Description\"}";
    private static final String KOTLIN_UPDATED = "{\"id\":\"kotlin\",\"subjectName\":\"Kotlin 2\",\"subjectDescription\":\"Updated\"}";
    private static final String ALL_TOPICS = "[" + SPRING + "," + JAVA + "," + JAVASCRIPT + "]";
    private static final String SORTED_TOPICS = "[" + JAVA + "," + JAVASCRIPT + "," + SPRING + "]";

    @Autowired
    private TestRestTemplate rest;

    @Test
    public void greetingDefaultsToWorld() {
        assertJson(rest.getForEntity("/", String.class), "{\"id\":1,\"content\":\"Hello, World!\"}");
    }

    @Test
    public void greetingUsesNameAndIncrementsCounter() {
        rest.getForEntity("/", String.class);
        assertJson(rest.getForEntity("/?name=Mateo", String.class), "{\"id\":2,\"content\":\"Hello, Mateo!\"}");
    }

    @Test
    public void listsAllTopics() {
        assertJson(rest.getForEntity("/topic", String.class), ALL_TOPICS);
    }

    @Test
    public void getsTopicById() {
        assertJson(rest.getForEntity("/topic/java", String.class), JAVA);
    }

    @Test
    public void filtersTopicsByMinimumIdLength() {
        assertJson(rest.getForEntity("/topic/minimum/length/4", String.class), "[" + SPRING + "," + JAVASCRIPT + "]");
    }

    @Test
    public void sortsTopicsInPlace() {
        assertJson(rest.getForEntity("/topic/sort", String.class), SORTED_TOPICS);
        assertJson(rest.getForEntity("/topic", String.class), SORTED_TOPICS);
    }

    @Test
    public void stringOperations() {
        ResponseEntity<String> response = rest.getForEntity("/topic/string/operation", String.class);
        assertText(response);
        assertEquals("Joining All String ID's with JOIN method: spring:java:javascript"
                + "-------------Get all ID characters, select distict and sort with ID=   :acgijnprstv"
                + "-------------Split All Id With Colon,Select ID With \"Java\" Keyword, Then Sort Then Join java:javascript"
                + "-------------Return All ID having character 'g' in it:  [spring]", response.getBody());
    }

    @Test
    public void fileOperations() {
        ResponseEntity<String> response = rest.getForEntity("/topic/file/operation", String.class);
        assertText(response);
        String body = response.getBody();
        assertTrue(body, body.startsWith("---------Find all files in path and sort:    "));
        assertTrue(body, body.contains("pom.xml; "));
        assertTrue(body, body.contains("temp.txt"));
        assertTrue(body, body.contains("sort : gradle; gradle/wrapper; gradle/wrapper/gradle-wrapper.jar; "
                + "gradle/wrapper/gradle-wrapper.properties; gradlew; gradlew.bat"));
        assertTrue(body, body.contains("with walk functiongradle; gradle/wrapper; "));
        assertTrue(body, body.endsWith("having \"print\" witin it:   Hello, this, is, Rehman"));
    }

    @Test
    public void dateTime() {
        ResponseEntity<String> response = rest.getForEntity("/datetime", String.class);
        assertText(response);
        String body = response.getBody();
        assertTrue(body, body.matches("Greetings from Spring Boot! -+Datetime now is \\S+-+Datetime tomorrow will be \\S+"
                + "-+Datetime of previous month was \\S+-+Is this a leap year \\?  (true|false)"
                + "-+Default system zone id   \\S+-+Time in California: \\S+\\[Canada/Central]"));
    }

    @Test
    public void addUpdateAndDeleteTopic() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> created = rest.postForEntity("/topic", new HttpEntity<>(KOTLIN, headers), String.class);
        assertEquals(HttpStatus.OK, created.getStatusCode());
        assertJson(rest.getForEntity("/topic/kotlin", String.class), KOTLIN);

        ResponseEntity<String> updated = rest.exchange("/topic/kotlin", HttpMethod.PUT,
                new HttpEntity<>(KOTLIN_UPDATED, headers), String.class);
        assertEquals(HttpStatus.OK, updated.getStatusCode());
        assertJson(rest.getForEntity("/topic/kotlin", String.class), KOTLIN_UPDATED);

        ResponseEntity<String> deleted = rest.exchange("/topic/kotlin", HttpMethod.DELETE, null, String.class);
        assertEquals(HttpStatus.OK, deleted.getStatusCode());
        assertJson(rest.getForEntity("/topic", String.class), ALL_TOPICS);
    }

    @Test
    public void unknownTopicIdIsServerError() {
        ResponseEntity<String> response = rest.getForEntity("/topic/does-not-exist", String.class);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody(), response.getBody().contains("\"status\":500"));
        assertTrue(response.getBody(), response.getBody().contains("\"path\":\"/topic/does-not-exist\""));
    }

    @Test
    public void trailingSlashMatchesSameEndpoint() {
        assertJson(rest.getForEntity("/topic/", String.class), ALL_TOPICS);
    }

    @Test
    public void servesStaticIndexPage() {
        ResponseEntity<String> response = rest.getForEntity("/index.html", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getHeaders().getContentType().isCompatibleWith(MediaType.TEXT_HTML));
        assertTrue(response.getBody().contains("<title>Title</title>"));
    }

    @Test
    public void unknownPathIsNotFound() {
        assertEquals(HttpStatus.NOT_FOUND, rest.getForEntity("/no/such/path", String.class).getStatusCode());
    }

    private static void assertJson(ResponseEntity<String> response, String expectedBody) {
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_JSON));
        assertEquals(expectedBody, response.getBody());
    }

    private static void assertText(ResponseEntity<String> response) {
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getHeaders().getContentType().isCompatibleWith(MediaType.TEXT_PLAIN));
    }
}
