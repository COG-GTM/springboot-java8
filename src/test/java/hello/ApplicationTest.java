package hello;

import java.util.List;

import hello.model.Customer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ApplicationTest {

    private static final String QUOTES_URL = "http://gturnquist-quoters.cfapps.io/api/random";

    private Application application;

    @BeforeEach
    void setUp() {
        application = new Application();
    }

    @Nested
    @DisplayName("restTemplate bean")
    class RestTemplateBean {

        @Test
        void buildsRestTemplateFromBuilder() {
            RestTemplate restTemplate = application.restTemplate(new RestTemplateBuilder());
            assertNotNull(restTemplate);
        }
    }

    @Nested
    @DisplayName("quotes CommandLineRunner")
    class QuotesRunner {

        private RestTemplate restTemplate;
        private MockRestServiceServer server;

        @BeforeEach
        void setUpServer() {
            restTemplate = new RestTemplate();
            server = MockRestServiceServer.createServer(restTemplate);
        }

        @Test
        void fetchesQuoteFromRemoteApi() throws Exception {
            String body = "{\"type\":\"success\",\"value\":{\"id\":10,\"quote\":\"Stay hungry, stay foolish\"}}";
            server.expect(requestTo(QUOTES_URL))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

            CommandLineRunner runner = application.run(restTemplate);
            runner.run();

            server.verify();
        }

        @Test
        void toleratesMissingValueInResponse() throws Exception {
            server.expect(requestTo(QUOTES_URL))
                    .andRespond(withSuccess("{\"type\":\"success\"}", MediaType.APPLICATION_JSON));

            CommandLineRunner runner = application.run(restTemplate);
            runner.run();

            server.verify();
        }

        @Test
        void propagatesServerErrorFromRemoteApi() throws Exception {
            server.expect(requestTo(QUOTES_URL))
                    .andRespond(withServerError());

            CommandLineRunner runner = application.run(restTemplate);

            assertThrows(HttpServerErrorException.class, runner::run);
            server.verify();
        }
    }

    @Nested
    @DisplayName("JDBC bootstrap")
    class JdbcBootstrap {

        private EmbeddedDatabase database;

        @BeforeEach
        void setUpDatabase() {
            database = new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.H2)
                    .generateUniqueName(true)
                    .build();
            application.jdbcTemplate = new JdbcTemplate(database);
        }

        @AfterEach
        void tearDown() {
            database.shutdown();
        }

        @Test
        void createsCustomersTableAndBatchInsertsSeedData() throws Exception {
            application.run();

            Integer count = application.jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM customers", Integer.class);
            assertEquals(Integer.valueOf(4), count);
        }

        @Test
        void splitsFullNamesIntoFirstAndLastNames() throws Exception {
            application.run();

            List<Customer> joshes = application.jdbcTemplate.query(
                    "SELECT id, first_name, last_name FROM customers WHERE first_name = ? ORDER BY last_name",
                    new Object[]{"Josh"},
                    (rs, rowNum) -> new Customer(
                            rs.getLong("id"), rs.getString("first_name"), rs.getString("last_name")));

            assertEquals(2, joshes.size());
            assertEquals("Bloch", joshes.get(0).getLastName());
            assertEquals("Long", joshes.get(1).getLastName());
        }

        @Test
        void isIdempotentBecauseTableIsDroppedIfExists() throws Exception {
            application.run();
            application.run();

            Integer count = application.jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM customers", Integer.class);
            assertEquals(Integer.valueOf(4), count);
        }
    }
}
