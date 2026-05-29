package hello.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class TopicTest {

    @Test
    public void testDefaultConstructor() {
        Topic topic = new Topic();
        assertNull(topic.getId());
        assertNull(topic.getSubjectName());
        assertNull(topic.getSubjectDescription());
    }

    @Test
    public void testParameterizedConstructor() {
        Topic topic = new Topic("java", "Core Java", "Java Description");
        assertEquals("java", topic.getId());
        assertEquals("Core Java", topic.getSubjectName());
        assertEquals("Java Description", topic.getSubjectDescription());
    }

    @Test
    public void testSettersAndGetters() {
        Topic topic = new Topic();
        topic.setId("spring");
        topic.setSubjectName("Spring Framework");
        topic.setSubjectDescription("Spring Description");

        assertEquals("spring", topic.getId());
        assertEquals("Spring Framework", topic.getSubjectName());
        assertEquals("Spring Description", topic.getSubjectDescription());
    }
}
