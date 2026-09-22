package hello.service;

import hello.exception.TopicNotFoundException;
import hello.model.Topic;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TopicServiceTest {

    private final TopicService topicService = new TopicService();

    @Test
    void getTopicWithIdReturnsKnownTopic() {
        Topic topic = topicService.getTopicWithId("java");

        assertEquals("java", topic.getId());
        assertEquals("Core Java", topic.getSubjectName());
    }

    @Test
    void getTopicWithIdThrowsNotFoundForUnknownId() {
        assertThrows(TopicNotFoundException.class, () -> topicService.getTopicWithId("does-not-exist"));
    }
}
