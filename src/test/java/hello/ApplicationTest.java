package hello;

import hello.model.Customer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ApplicationTest {

    private EmbeddedDatabase database;

    @Before
    public void setUp() {
        database = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .build();
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void runCreatesAndSeedsCustomers() throws Exception {
        Application application = new Application();
        application.jdbcTemplate = new JdbcTemplate(database);

        application.run();

        assertEquals(4, application.jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM customers", Integer.class).intValue());
        List<Customer> joshes = application.jdbcTemplate.query(
                "SELECT id, first_name, last_name FROM customers WHERE first_name = ?",
                new Object[]{"Josh"},
                (rs, rowNum) -> new Customer(rs.getLong("id"),
                        rs.getString("first_name"), rs.getString("last_name")));
        assertEquals(2, joshes.size());
    }

    @Test
    public void createsRestTemplate() {
        assertNotNull(new Application().restTemplate(new RestTemplateBuilder()));
    }
}
