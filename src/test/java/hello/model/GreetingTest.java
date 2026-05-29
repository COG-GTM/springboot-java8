package hello.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class GreetingTest {

    @Test
    public void testConstructorAndGetters() {
        Greeting greeting = new Greeting(1L, "Hello, World!");
        assertEquals(1L, greeting.getId());
        assertEquals("Hello, World!", greeting.getContent());
    }

    @Test
    public void testDifferentValues() {
        Greeting greeting = new Greeting(42L, "Hi there");
        assertEquals(42L, greeting.getId());
        assertEquals("Hi there", greeting.getContent());
    }
}
