package hello.model;

import static org.junit.Assert.*;

import org.junit.Test;

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
        Value value = new Value();
        value.setId(1L);
        value.setQuote("test quote");

        quote.setType("success");
        quote.setValue(value);

        assertEquals("success", quote.getType());
        assertNotNull(quote.getValue());
        assertEquals("test quote", quote.getValue().getQuote());
    }

    @Test
    public void testToString() {
        Quote quote = new Quote();
        quote.setType("success");
        String str = quote.toString();
        assertTrue(str.contains("success"));
    }
}
