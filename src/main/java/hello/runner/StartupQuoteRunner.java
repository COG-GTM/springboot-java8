package hello.runner;

import hello.service.QuoteClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Logs a random quote on startup when a quote service is configured.
 */
@Component
public class StartupQuoteRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupQuoteRunner.class);

    private final QuoteClient quoteClient;

    public StartupQuoteRunner(QuoteClient quoteClient) {
        this.quoteClient = quoteClient;
    }

    @Override
    public void run(String... args) {
        quoteClient.randomQuote()
                .ifPresentOrElse(
                        quote -> log.info(quote.toString()),
                        () -> log.info("No quote available"));
    }
}
