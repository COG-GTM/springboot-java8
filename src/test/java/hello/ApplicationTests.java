package hello;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ApplicationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void customerTableIsCreatedAndSeededOnStartup() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM customers", Integer.class);
        assertThat(count).isEqualTo(4);
    }

    @Test
    void identityColumnGeneratesIds() {
        Long minId = jdbcTemplate.queryForObject("SELECT MIN(id) FROM customers", Long.class);
        assertThat(minId).isNotNull().isPositive();
    }
}
