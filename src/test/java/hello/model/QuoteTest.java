package hello.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class QuoteTest {

    @Test
    public void testDefaultConstructor() {
        Quote quote = new Quote();
        assertNull(quote.getType());
        assertNull(quote.getValue());
    }

    @Test
    public void testSettersAndGetters() {
        Quote quote = new Quote();
        quote.setType("success");

        Value value = new Value();
        value.setId(1L);
        value.setQuote("Test quote");
        quote.setValue(value);

        assertEquals("success", quote.getType());
        assertNotNull(quote.getValue());
        assertEquals("Test quote", quote.getValue().getQuote());
    }

    @Test
    public void testToString() {
        Quote quote = new Quote();
        quote.setType("success");

        Value value = new Value();
        value.setId(1L);
        value.setQuote("Test");
        quote.setValue(value);

        String result = quote.toString();
        assertTrue(result.contains("success"));
        assertTrue(result.contains("Value{"));
    }
}
