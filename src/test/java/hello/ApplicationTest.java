package hello;

import hello.model.Quote;
import hello.model.Value;
import org.junit.Test;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ApplicationTest {

    @Test
    public void testRestTemplateBean() {
        Application app = new Application();
        RestTemplateBuilder builder = new RestTemplateBuilder();
        RestTemplate rt = app.restTemplate(builder);
        assertNotNull(rt);
    }

    @Test
    public void testCommandLineRunnerBean() throws Exception {
        Application app = new Application();
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        Quote quote = new Quote();
        quote.setType("success");
        Value value = new Value();
        value.setId(1L);
        value.setQuote("Test");
        quote.setValue(value);
        when(mockRestTemplate.getForObject(anyString(), eq(Quote.class))).thenReturn(quote);

        CommandLineRunner runner = app.run(mockRestTemplate);
        assertNotNull(runner);
        runner.run();
        verify(mockRestTemplate).getForObject(anyString(), eq(Quote.class));
    }

    @Test
    public void testRunJdbcOperations() throws Exception {
        DataSource dataSource = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .build();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        Application app = new Application();
        java.lang.reflect.Field field = Application.class.getDeclaredField("jdbcTemplate");
        field.setAccessible(true);
        field.set(app, jdbcTemplate);

        app.run(new String[]{});

        java.util.List<hello.model.Customer> customers = jdbcTemplate.query(
                "SELECT id, first_name, last_name FROM customers WHERE first_name = ?",
                new Object[]{"Josh"},
                (rs, rowNum) -> new hello.model.Customer(
                        rs.getLong("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name")
                )
        );
        assertEquals(2, customers.size());
    }
}
