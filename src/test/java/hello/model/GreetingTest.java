package hello.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class GreetingTest {

    @Test
    public void testConstructorAndGetters() {
        Greeting greeting = new Greeting(1L, "Hello, World!");
        assertEquals(1L, greeting.getId());
        assertEquals("Hello, World!", greeting.getContent());
    }

    @Test
    public void testDifferentValues() {
        Greeting greeting = new Greeting(42L, "Test content");
        assertEquals(42L, greeting.getId());
        assertEquals("Test content", greeting.getContent());
    }
}
