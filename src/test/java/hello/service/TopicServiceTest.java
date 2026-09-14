package hello.service;

import hello.model.Topic;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TopicServiceTest {

    private TopicService service;

    @Before
    public void setUp() {
        service = new TopicService();
    }

    @Test
    public void getTopicWithId() {
        assertEquals("Spring Framework", service.getTopicWithId("spring").getSubjectName());
    }

    @Test
    public void updateTopicReplacesMatchingId() {
        service.updateTopic("java", new Topic("java", "Java 21", "Modern Java"));
        assertEquals("Java 21", service.getTopicWithId("java").getSubjectName());
    }

    @Test
    public void updateTopicIgnoresUnknownId() {
        service.updateTopic("nope", new Topic("nope", "x", "y"));
        assertEquals(3, service.getAllTopics().size());
    }

    @Test
    public void deleteTopic() {
        service.deleteTopic("spring");
        assertEquals(2, service.getAllTopics().size());
    }

    @Test
    public void filterMinimumLengthForId() {
        List<Topic> result = service.filterMinimumLengthForId(4);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(t -> t.getId().length() > 4));
    }

    @Test
    public void stringOperations() {
        String join = service.returnAllTopicIDWithStringSlicing();
        assertEquals("spring:java:javascript", join);
        assertEquals(":acgijnprstv", service.makeDistinctAndSortCharacters(join));
        assertEquals("java:javascript", service.splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoin(join));
        assertEquals("[spring]", service.findIdHavingCharacter());
    }

    @Test
    public void readFileWithStreamFunction() {
        assertEquals(" Hello, this, is, Rehman", service.readFileWithStreamFunction());
    }
}
