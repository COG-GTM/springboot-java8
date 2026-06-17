package hello;

import hello.model.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JdbcIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void customersTableExistsAndHasData() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM customers", Integer.class);
        assertNotNull(count);
        assertTrue(count >= 4, "Expected at least 4 customers from CommandLineRunner seeding");
    }

    @Test
    void canQueryCustomersByFirstName() {
        List<Customer> joshCustomers = jdbcTemplate.query(
                "SELECT id, first_name, last_name FROM customers WHERE first_name = ?",
                (rs, rowNum) -> new Customer(rs.getLong("id"), rs.getString("first_name"), rs.getString("last_name")),
                "Josh"
        );
        assertEquals(2, joshCustomers.size());
        for (Customer c : joshCustomers) {
            assertEquals("Josh", c.getFirstName());
        }
    }

    @Test
    void allSeededCustomersPresent() {
        List<String> firstNames = jdbcTemplate.queryForList(
                "SELECT first_name FROM customers ORDER BY first_name", String.class);
        assertEquals(4, firstNames.size());
        assertTrue(firstNames.contains("Jeff"));
        assertTrue(firstNames.contains("John"));
        assertTrue(firstNames.contains("Josh"));
    }
}
