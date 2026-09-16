package hello.service;

import java.util.Optional;

import hello.model.Quote;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class QuoteClientTest {

    private static final String URL = "https://quotes.example.com/api/random";

    private final RestTemplate restTemplate = new RestTemplate();
    private final MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);

    @Test
    void returnsQuoteFromConfiguredUrl() {
        server.expect(requestTo(URL))
                .andRespond(withSuccess("{\"type\":\"success\",\"value\":{\"id\":1,\"quote\":\"Spring Boot\"}}",
                        MediaType.APPLICATION_JSON));

        Optional<Quote> quote = new QuoteClient(restTemplate, URL).randomQuote();

        assertThat(quote).isPresent();
        assertThat(quote.get().getValue().getQuote()).isEqualTo("Spring Boot");
        server.verify();
    }

    @Test
    void returnsEmptyWhenServiceFails() {
        server.expect(requestTo(URL)).andRespond(withServerError());

        assertThat(new QuoteClient(restTemplate, URL).randomQuote()).isEmpty();
        server.verify();
    }

    @Test
    void returnsEmptyAndSkipsCallWhenUrlNotConfigured() {
        assertThat(new QuoteClient(restTemplate, "").randomQuote()).isEmpty();
        server.verify();
    }
}
