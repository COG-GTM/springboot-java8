package hello.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class ValueTest {

    @Test
    public void testDefaultConstructor() {
        Value value = new Value();
        assertNull(value.getId());
        assertNull(value.getQuote());
    }

    @Test
    public void testSettersAndGetters() {
        Value value = new Value();
        value.setId(1L);
        value.setQuote("Test quote");
        assertEquals(Long.valueOf(1L), value.getId());
        assertEquals("Test quote", value.getQuote());
    }

    @Test
    public void testToString() {
        Value value = new Value();
        value.setId(1L);
        value.setQuote("Hello");
        String result = value.toString();
        assertTrue(result.contains("1"));
        assertTrue(result.contains("Hello"));
    }
}
