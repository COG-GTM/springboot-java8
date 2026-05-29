package hello.model;

import static org.junit.Assert.*;

import org.junit.Test;

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
        value.setId(42L);
        value.setQuote("Hello World");

        assertEquals(Long.valueOf(42L), value.getId());
        assertEquals("Hello World", value.getQuote());
    }

    @Test
    public void testToString() {
        Value value = new Value();
        value.setId(1L);
        value.setQuote("test");
        String str = value.toString();
        assertTrue(str.contains("1"));
        assertTrue(str.contains("test"));
    }
}
