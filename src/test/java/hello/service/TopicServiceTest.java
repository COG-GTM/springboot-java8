package hello.service;

import hello.model.Topic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class TopicServiceTest {

    private TopicService topicService;

    @BeforeEach
    void setUp() {
        topicService = new TopicService();
    }

    @Test
    void getAllTopics_returnsDefaultTopics() {
        List<Topic> topics = topicService.getAllTopics();
        assertEquals(3, topics.size());
    }

    @Test
    void getTopicWithId_existingId_returnsTopic() {
        Topic topic = topicService.getTopicWithId("java");
        assertNotNull(topic);
        assertEquals("java", topic.getId());
        assertEquals("Core Java", topic.getSubjectName());
    }

    @Test
    void getTopicWithId_nonExistingId_throwsException() {
        assertThrows(NoSuchElementException.class, () -> topicService.getTopicWithId("nonexistent"));
    }

    @Test
    void addTopic_increasesListSize() {
        Topic newTopic = new Topic("python", "Python", "Python Description");
        topicService.addTopic(newTopic);
        assertEquals(4, topicService.getAllTopics().size());
        assertEquals("python", topicService.getTopicWithId("python").getId());
    }

    @Test
    void updateTopic_existingId_updatesTopic() {
        Topic updatedTopic = new Topic("java", "Advanced Java", "Advanced Java Description");
        topicService.updateTopic("java", updatedTopic);
        Topic result = topicService.getTopicWithId("java");
        assertEquals("Advanced Java", result.getSubjectName());
    }

    @Test
    void deleteTopic_existingId_removesFromList() {
        topicService.deleteTopic("java");
        assertEquals(2, topicService.getAllTopics().size());
        assertThrows(NoSuchElementException.class, () -> topicService.getTopicWithId("java"));
    }

    @Test
    void sortTopicsWithID_returnsSortedList() {
        List<Topic> sorted = topicService.sortTopicsWithID();
        assertEquals("java", sorted.get(0).getId());
        assertEquals("javascript", sorted.get(1).getId());
        assertEquals("spring", sorted.get(2).getId());
    }

    @Test
    void filterMinimumLengthForId_returnsFilteredList() {
        List<Topic> filtered = topicService.filterMinimumLengthForId(4);
        assertEquals(2, filtered.size());
    }

    @Test
    void returnAllTopicIDWithStringSlicing_returnsJoinedIds() {
        String result = topicService.returnAllTopicIDWithStringSlicing();
        assertTrue(result.contains("spring"));
        assertTrue(result.contains("java"));
        assertTrue(result.contains(":"));
    }
}
