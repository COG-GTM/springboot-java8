package hello.controller;

import hello.model.Greeting;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GreetingControllerTest {

    @Test
    void greetingUsesDefaultNameAndIncrementsCounter() {
        GreetingController controller = new GreetingController();

        Greeting first = controller.greeting("World");
        assertEquals(1L, first.getId());
        assertEquals("Hello, World!", first.getContent());

        Greeting second = controller.greeting("World");
        assertEquals(2L, second.getId());
    }

    @Test
    void greetingUsesProvidedName() {
        GreetingController controller = new GreetingController();
        Greeting greeting = controller.greeting("Matthew");
        assertEquals("Hello, Matthew!", greeting.getContent());
    }
}
