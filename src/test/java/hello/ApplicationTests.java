package hello;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class ApplicationTests {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
        assertThat(context.getBean(Application.class)).isNotNull();
    }

    @Test
    void commandLineRunnerPopulatesCustomersTable() {
        Integer total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM customers", Integer.class);
        Integer joshes = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM customers WHERE first_name = ?", Integer.class, "Josh");
        assertThat(total).isEqualTo(4);
        assertThat(joshes).isEqualTo(2);
    }
}
