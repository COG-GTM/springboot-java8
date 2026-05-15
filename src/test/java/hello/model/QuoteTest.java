package hello.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class QuoteTest {

    @Test
    public void noArgConstructorAndGettersSetters() {
        Quote quote = new Quote();
        assertNull(quote.getType());
        assertNull(quote.getValue());

        Value value = new Value();
        value.setId(1L);
        value.setQuote("test quote");

        quote.setType("success");
        quote.setValue(value);

        assertEquals("success", quote.getType());
        assertEquals(value, quote.getValue());
    }

    @Test
    public void toString_containsTypeAndValue() {
        Quote quote = new Quote();
        quote.setType("success");
        Value value = new Value();
        value.setId(1L);
        value.setQuote("test");
        quote.setValue(value);

        String result = quote.toString();
        assertTrue(result.contains("type='success'"));
        assertTrue(result.contains("value="));
        assertTrue(result.startsWith("Quote{"));
    }
}
