package hello;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Replaces the external quote service (gturnquist-quoters.cfapps.io, no longer reachable)
 * that the startup CommandLineRunner calls, so the application context can start in tests.
 */
@TestConfiguration
public class QuoteServiceStub {

    static final String QUOTE_URL = "http://gturnquist-quoters.cfapps.io/api/random";
    static final String QUOTE_JSON = "{\"type\":\"success\",\"value\":{\"id\":10,"
            + "\"quote\":\"Really loving Spring Boot, makes stand alone Spring apps easy.\"}}";

    private final RestTemplate restTemplate = new RestTemplate();
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

    public QuoteServiceStub() {
        server.expect(ExpectedCount.manyTimes(), requestTo(QUOTE_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(QUOTE_JSON, MediaType.APPLICATION_JSON));
    }

    @Bean
    @Primary
    public RestTemplate quoteRestTemplate() {
        return restTemplate;
    }

    @Bean
    public MockRestServiceServer quoteServer() {
        return server;
    }
}
