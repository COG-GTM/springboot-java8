package hello.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelPojoTest {

    @Test
    void topicAllArgsConstructorAndGetters() {
        Topic topic = new Topic("java", "Core Java", "Java Description");
        assertEquals("java", topic.getId());
        assertEquals("Core Java", topic.getSubjectName());
        assertEquals("Java Description", topic.getSubjectDescription());
    }

    @Test
    void topicNoArgsConstructorAndSetters() {
        Topic topic = new Topic();
        topic.setId("go");
        topic.setSubjectName("Go Lang");
        topic.setSubjectDescription("Go Description");
        assertEquals("go", topic.getId());
        assertEquals("Go Lang", topic.getSubjectName());
        assertEquals("Go Description", topic.getSubjectDescription());
    }

    @Test
    void greetingIsImmutable() {
        Greeting greeting = new Greeting(42L, "Hello, World!");
        assertEquals(42L, greeting.getId());
        assertEquals("Hello, World!", greeting.getContent());
    }

    @Test
    void customerGettersSettersAndToString() {
        Customer customer = new Customer(1L, "Josh", "Bloch");
        assertEquals(1L, customer.getId());
        assertEquals("Josh", customer.getFirstName());
        assertEquals("Bloch", customer.getLastName());

        customer.setId(2L);
        customer.setFirstName("Jeff");
        customer.setLastName("Dean");
        assertEquals(2L, customer.getId());
        assertEquals("Jeff", customer.getFirstName());
        assertEquals("Dean", customer.getLastName());

        String text = customer.toString();
        assertTrue(text.contains("Jeff"));
        assertTrue(text.contains("Dean"));
        assertTrue(text.contains("id=2"));
    }

    @Test
    void valueGettersSettersAndToString() {
        Value value = new Value();
        value.setId(7L);
        value.setQuote("Stay hungry");
        assertEquals(Long.valueOf(7L), value.getId());
        assertEquals("Stay hungry", value.getQuote());
        assertTrue(value.toString().contains("Stay hungry"));
    }

    @Test
    void quoteGettersSettersAndToString() {
        Value value = new Value();
        value.setId(1L);
        value.setQuote("Work hard");

        Quote quote = new Quote();
        quote.setType("success");
        quote.setValue(value);

        assertEquals("success", quote.getType());
        assertEquals(value, quote.getValue());
        assertTrue(quote.toString().contains("success"));
        assertTrue(quote.toString().contains("Work hard"));
    }
}
