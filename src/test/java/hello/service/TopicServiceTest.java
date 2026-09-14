package hello.service;

import hello.model.Topic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TopicServiceTest {

    private TopicService service;

    @BeforeEach
    void setUp() {
        service = new TopicService();
    }

    @Test
    void getTopicWithId() {
        assertEquals("Spring Framework", service.getTopicWithId("spring").getSubjectName());
    }

    @Test
    void updateTopicReplacesMatchingId() {
        service.updateTopic("java", new Topic("java", "Java 21", "Modern Java"));
        assertEquals("Java 21", service.getTopicWithId("java").getSubjectName());
    }

    @Test
    void updateTopicIgnoresUnknownId() {
        service.updateTopic("nope", new Topic("nope", "x", "y"));
        assertEquals(3, service.getAllTopics().size());
    }

    @Test
    void deleteTopic() {
        service.deleteTopic("spring");
        assertEquals(2, service.getAllTopics().size());
    }

    @Test
    void filterMinimumLengthForId() {
        List<Topic> result = service.filterMinimumLengthForId(4);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(t -> t.getId().length() > 4));
    }

    @Test
    void stringOperations() {
        String join = service.returnAllTopicIDWithStringSlicing();
        assertEquals("spring:java:javascript", join);
        assertEquals(":acgijnprstv", service.makeDistinctAndSortCharacters(join));
        assertEquals("java:javascript", service.splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoin(join));
        assertEquals("[spring]", service.findIdHavingCharacter());
    }

    @Test
    void readFileWithStreamFunction() {
        assertEquals(" Hello, this, is, Rehman", service.readFileWithStreamFunction());
    }
}
