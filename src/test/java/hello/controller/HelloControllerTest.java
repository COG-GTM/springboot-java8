package hello.controller;

import hello.service.TopicService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HelloControllerTest {

    private HelloController controller;

    @BeforeEach
    void setUp() throws Exception {
        controller = new HelloController();
        Field field = HelloController.class.getDeclaredField("topicService");
        field.setAccessible(true);
        field.set(controller, new TopicService());
    }

    @Test
    void indexReturnsDateTimeGreeting() {
        String result = controller.index();
        assertNotNull(result);
        assertTrue(result.startsWith("Greetings from Spring Boot!"));
        assertTrue(result.contains("Is this a leap year ?"));
    }

    @Test
    void showStringOperationCombinesServiceResults() {
        String result = controller.showStringOperation();
        assertTrue(result.contains("spring:java:javascript"));
        assertTrue(result.contains("java:javascript"));
        assertTrue(result.contains("[spring]"));
    }

    @Test
    void showFileOperationReturnsCombinedFileResults() {
        String result = controller.showFileOperation();
        assertNotNull(result);
        assertFalse(result.contains("IO exception"));
        assertTrue(result.contains("Find all files in path and sort"));
    }
}
