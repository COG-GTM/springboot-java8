package hello.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class ValueTest {

    @Test
    public void noArgConstructorAndGettersSetters() {
        Value value = new Value();
        assertNull(value.getId());
        assertNull(value.getQuote());

        value.setId(1L);
        value.setQuote("test quote");

        assertEquals(Long.valueOf(1L), value.getId());
        assertEquals("test quote", value.getQuote());
    }

    @Test
    public void toString_containsIdAndQuote() {
        Value value = new Value();
        value.setId(42L);
        value.setQuote("Spring is great");

        String result = value.toString();
        assertTrue(result.contains("id=42"));
        assertTrue(result.contains("quote='Spring is great'"));
        assertTrue(result.startsWith("Value{"));
    }
}
