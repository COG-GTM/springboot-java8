package hello.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class CustomerTest {

    @Test
    public void testConstructorAndGetters() {
        Customer customer = new Customer(1L, "John", "Doe");
        assertEquals(1L, customer.getId());
        assertEquals("John", customer.getFirstName());
        assertEquals("Doe", customer.getLastName());
    }

    @Test
    public void testSetters() {
        Customer customer = new Customer(1L, "John", "Doe");
        customer.setId(2L);
        customer.setFirstName("Jane");
        customer.setLastName("Smith");
        assertEquals(2L, customer.getId());
        assertEquals("Jane", customer.getFirstName());
        assertEquals("Smith", customer.getLastName());
    }

    @Test
    public void testToString() {
        Customer customer = new Customer(1L, "John", "Doe");
        String str = customer.toString();
        assertTrue(str.contains("John"));
        assertTrue(str.contains("Doe"));
        assertTrue(str.contains("1"));
    }
}
