package hello;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ApplicationTests {

    @Autowired
    private Application application;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
        assertThat(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).isEqualTo(1);
    }

    @Test
    void commandLineRunnerPopulatesCustomers() throws Exception {
        application.run();

        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM customers", Integer.class)).isEqualTo(4);
    }
}
