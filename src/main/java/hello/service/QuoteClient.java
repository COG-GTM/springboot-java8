package hello.service;

import java.util.Optional;

import hello.model.Quote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Fetches a random quote from an external service.
 */
@Service
public class QuoteClient {

    private static final Logger log = LoggerFactory.getLogger(QuoteClient.class);

    private final RestTemplate restTemplate;
    private final String quoteUrl;

    public QuoteClient(RestTemplate restTemplate, @Value("${quotes.api.url:}") String quoteUrl) {
        this.restTemplate = restTemplate;
        this.quoteUrl = quoteUrl;
    }

    public Optional<Quote> randomQuote() {
        if (!StringUtils.hasText(quoteUrl)) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(restTemplate.getForObject(quoteUrl, Quote.class));
        } catch (RestClientException ex) {
            log.warn("Could not fetch a quote from {}: {}", quoteUrl, ex.getMessage());
            return Optional.empty();
        }
    }
}
