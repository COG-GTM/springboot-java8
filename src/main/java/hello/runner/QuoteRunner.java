package hello.runner;

import hello.config.QuoteProperties;
import hello.model.Quote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@ConditionalOnProperty(prefix = "app.quote", name = "enabled", havingValue = "true")
public class QuoteRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(QuoteRunner.class);

    private final RestTemplate restTemplate;

    private final QuoteProperties properties;

    public QuoteRunner(RestTemplate restTemplate, QuoteProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    @Override
    public void run(String... args) {
        String url = properties.getUrl();
        try {
            Quote quote = restTemplate.getForObject(url, Quote.class);
            if (quote == null) {
                log.warn("Quote service {} returned an empty response", url);
                return;
            }
            log.info(quote.toString());
        } catch (RestClientException ex) {
            log.warn("Unable to fetch a quote from {}: {}", url, ex.getMessage());
        }
    }
}
