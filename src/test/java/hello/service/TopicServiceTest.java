package hello.service;

import hello.model.Topic;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TopicServiceTest {

    private TopicService topicService;

    @Before
    public void setUp() {
        topicService = new TopicService();
    }

    private static String repeat(char c, int count) {
        return String.join("", Collections.nCopies(count, String.valueOf(c)));
    }

    @Test
    public void findIdHavingCharacterReturnsIdsContainingG() {
        assertEquals("[spring]", topicService.findIdHavingCharacter());

        topicService.addTopic(new Topic("golang", "Go", "Go Description"));
        topicService.addTopic(new Topic(repeat('a', Topic.MAX_ID_LENGTH), "a", "a"));

        assertEquals("[spring, golang]", topicService.findIdHavingCharacter());
    }

    @Test(timeout = 2000)
    public void findIdHavingCharacterIsLinearInIdLength() {
        topicService.addTopic(new Topic(repeat('a', 500_000), "a", "a"));

        assertEquals("[spring]", topicService.findIdHavingCharacter());
    }

    @Test
    public void addTopicRejectsTopicsBeyondCapacity() {
        int initialSize = topicService.getAllTopics().size();
        for (int i = initialSize; i < TopicService.MAX_TOPICS; i++) {
            topicService.addTopic(new Topic("id" + i, "name", "description"));
        }
        assertEquals(TopicService.MAX_TOPICS, topicService.getAllTopics().size());

        try {
            topicService.addTopic(new Topic("overflow", "name", "description"));
            fail("Expected TopicStoreFullException");
        } catch (TopicStoreFullException expected) {
            assertEquals(TopicService.MAX_TOPICS, topicService.getAllTopics().size());
        }

        topicService.deleteTopic("id" + initialSize);
        topicService.addTopic(new Topic("replacement", "name", "description"));
        assertEquals(TopicService.MAX_TOPICS, topicService.getAllTopics().size());
    }
}
