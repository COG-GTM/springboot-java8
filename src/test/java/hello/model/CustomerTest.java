package hello.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class CustomerTest {

    @Test
    public void constructorAndGetters() {
        Customer customer = new Customer(1L, "John", "Doe");
        assertEquals(1L, customer.getId());
        assertEquals("John", customer.getFirstName());
        assertEquals("Doe", customer.getLastName());
    }

    @Test
    public void setters() {
        Customer customer = new Customer(1L, "John", "Doe");
        customer.setId(2L);
        customer.setFirstName("Jane");
        customer.setLastName("Smith");

        assertEquals(2L, customer.getId());
        assertEquals("Jane", customer.getFirstName());
        assertEquals("Smith", customer.getLastName());
    }

    @Test
    public void toString_containsAllFields() {
        Customer customer = new Customer(1L, "John", "Doe");
        String result = customer.toString();
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("firstName='John'"));
        assertTrue(result.contains("lastName='Doe'"));
        assertTrue(result.startsWith("Customer{"));
    }
}
