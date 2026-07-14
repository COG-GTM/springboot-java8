package hello.controller;

import hello.model.Topic;
import hello.service.TopicService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TopicControllerTest {

    private TopicController controller;

    @BeforeEach
    void setUp() throws Exception {
        controller = new TopicController();
        Field field = TopicController.class.getDeclaredField("topicService");
        field.setAccessible(true);
        field.set(controller, new TopicService());
    }

    private List<String> ids(List<Topic> topics) {
        return topics.stream().map(Topic::getId).collect(Collectors.toList());
    }

    @Test
    void getAllTopicsDelegatesToService() {
        assertEquals(Arrays.asList("spring", "java", "javascript"), ids(controller.getAllTopics()));
    }

    @Test
    void getTopicWithIdDelegatesToService() {
        assertEquals("Core Java", controller.getTopicWithID("java").getSubjectName());
    }

    @Test
    void addTopicDelegatesToService() {
        controller.addTopic(new Topic("go", "Go Lang", "Go Description"));
        assertEquals(4, controller.getAllTopics().size());
    }

    @Test
    void updateTopicDelegatesToService() {
        controller.updateTopic("java", new Topic("java", "Modern Java", "Updated"));
        assertEquals("Modern Java", controller.getTopicWithID("java").getSubjectName());
    }

    @Test
    void deleteTopicDelegatesToService() {
        controller.deleteTopic("java");
        assertFalse(ids(controller.getAllTopics()).contains("java"));
    }

    @Test
    void filterMinimumLengthForIdDelegatesToService() {
        assertEquals(Arrays.asList("javascript"), ids(controller.filterMinimumLengthForId(6)));
    }

    @Test
    void sortTopicsWithIdDelegatesToService() {
        assertEquals(Arrays.asList("java", "javascript", "spring"), ids(controller.sortTopicsWithID()));
    }
}
