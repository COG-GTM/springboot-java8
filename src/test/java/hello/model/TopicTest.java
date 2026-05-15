package hello.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class TopicTest {

    @Test
    public void noArgConstructorAndSetters() {
        Topic topic = new Topic();
        topic.setId("test");
        topic.setSubjectName("Test Subject");
        topic.setSubjectDescription("Test Description");

        assertEquals("test", topic.getId());
        assertEquals("Test Subject", topic.getSubjectName());
        assertEquals("Test Description", topic.getSubjectDescription());
    }

    @Test
    public void allArgsConstructorAndGetters() {
        Topic topic = new Topic("spring", "Spring Framework", "Spring Desc");

        assertEquals("spring", topic.getId());
        assertEquals("Spring Framework", topic.getSubjectName());
        assertEquals("Spring Desc", topic.getSubjectDescription());
    }

    @Test
    public void settersOverwritePreviousValues() {
        Topic topic = new Topic("old", "Old Name", "Old Desc");
        topic.setId("new");
        topic.setSubjectName("New Name");
        topic.setSubjectDescription("New Desc");

        assertEquals("new", topic.getId());
        assertEquals("New Name", topic.getSubjectName());
        assertEquals("New Desc", topic.getSubjectDescription());
    }
}
