package hello;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.client.MockRestServiceServer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Import(QuoteServiceStub.class)
public class ApplicationStartupTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MockRestServiceServer quoteServer;

    @Test
    public void startupRunnerCreatesAndLoadsCustomersTable() {
        Integer total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM customers", Integer.class);
        assertEquals(Integer.valueOf(4), total);

        List<String> joshes = jdbcTemplate.queryForList(
                "SELECT last_name FROM customers WHERE first_name = 'Josh' ORDER BY id", String.class);
        assertEquals(2, joshes.size());
        assertEquals("Bloch", joshes.getFirst());
        assertEquals("Long", joshes.get(1));
    }

    @Test
    public void customerIdsAreGeneratedSequentially() {
        List<Long> ids = jdbcTemplate.queryForList("SELECT id FROM customers ORDER BY id", Long.class);
        assertEquals(4, ids.size());
        assertEquals(Long.valueOf(1), ids.getFirst());
        assertEquals(Long.valueOf(4), ids.get(3));
    }

    @Test
    public void startupRunnerFetchesQuote() {
        quoteServer.verify();
    }
}
