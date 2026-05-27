package hello;

import hello.model.Customer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JdbcIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void customersTableExistsAndHasData() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM customers", Integer.class);
        assertNotNull(count);
        assertTrue("Expected at least 4 customers from CommandLineRunner seeding", count >= 4);
    }

    @Test
    public void canQueryCustomersByFirstName() {
        List<Customer> joshCustomers = jdbcTemplate.query(
                "SELECT id, first_name, last_name FROM customers WHERE first_name = ?",
                new Object[]{"Josh"},
                (rs, rowNum) -> new Customer(rs.getLong("id"), rs.getString("first_name"), rs.getString("last_name"))
        );
        assertEquals(2, joshCustomers.size());
        for (Customer c : joshCustomers) {
            assertEquals("Josh", c.getFirstName());
        }
    }

    @Test
    public void allSeededCustomersPresent() {
        List<String> firstNames = jdbcTemplate.queryForList(
                "SELECT first_name FROM customers ORDER BY first_name", String.class);
        assertEquals(4, firstNames.size());
        assertTrue(firstNames.contains("Jeff"));
        assertTrue(firstNames.contains("John"));
        assertTrue(firstNames.contains("Josh"));
    }
}
