package hello.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ModelsTest {

    @Test
    public void topicSupportsConstructorsAndProperties() {
        Topic topic = new Topic("id", "name", "description");
        assertEquals("id", topic.getId());
        assertEquals("name", topic.getSubjectName());
        assertEquals("description", topic.getSubjectDescription());

        topic.setId("new-id");
        topic.setSubjectName("new-name");
        topic.setSubjectDescription("new-description");
        assertEquals("new-id", topic.getId());
        assertEquals("new-name", topic.getSubjectName());
        assertEquals("new-description", topic.getSubjectDescription());
    }

    @Test
    public void customerSupportsPropertiesAndToString() {
        Customer customer = new Customer(7L, "Ada", "Lovelace");
        assertEquals(7L, customer.getId());
        assertEquals("Ada", customer.getFirstName());
        assertEquals("Lovelace", customer.getLastName());
        assertEquals("Customer{id=7, firstName='Ada', lastName='Lovelace'}", customer.toString());

        customer.setId(8L);
        customer.setFirstName("Grace");
        customer.setLastName("Hopper");
        assertEquals(8L, customer.getId());
        assertEquals("Grace", customer.getFirstName());
        assertEquals("Hopper", customer.getLastName());
    }

    @Test
    public void greetingExposesConstructorValues() {
        Greeting greeting = new Greeting(3L, "Hello");

        assertEquals(3L, greeting.getId());
        assertEquals("Hello", greeting.getContent());
    }

    @Test
    public void quoteSupportsPropertiesAndToString() {
        Quote quote = new Quote();
        Value value = new Value();
        value.setId(11L);
        value.setQuote("A quote");
        quote.setType("success");
        quote.setValue(value);

        assertEquals("success", quote.getType());
        assertEquals(value, quote.getValue());
        assertEquals("Quote{type='success', value=Value{id=11, quote='A quote'}}", quote.toString());
    }

    @Test
    public void valueSupportsPropertiesAndToString() {
        Value value = new Value();
        value.setId(12L);
        value.setQuote("Text");

        assertEquals(12L, value.getId().longValue());
        assertEquals("Text", value.getQuote());
        assertEquals("Value{id=12, quote='Text'}", value.toString());
    }
}
