package hello.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class GreetingTest {

    @Test
    public void constructorAndGetters() {
        Greeting greeting = new Greeting(1L, "Hello, World!");
        assertEquals(1L, greeting.getId());
        assertEquals("Hello, World!", greeting.getContent());
    }

    @Test
    public void differentValues() {
        Greeting greeting = new Greeting(42L, "Hello, Devin!");
        assertEquals(42L, greeting.getId());
        assertEquals("Hello, Devin!", greeting.getContent());
    }
}
