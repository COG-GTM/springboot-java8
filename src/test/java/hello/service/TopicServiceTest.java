package hello.service;

import hello.model.Topic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TopicServiceTest {

    private TopicService service;

    @BeforeEach
    public void setUp() {
        service = new TopicService();
    }

    @Test
    public void startsWithThreeHardcodedTopics() {
        assertEquals("spring:java:javascript", ids(service.getAllTopics()));
    }

    @Test
    public void updateIgnoresUnknownId() {
        service.updateTopic("missing", new Topic("missing", "x", "y"));
        assertEquals("spring:java:javascript", ids(service.getAllTopics()));
    }

    @Test
    public void updateReplacesTopicAtSamePosition() {
        service.updateTopic("java", new Topic("java", "Java 21", "LTS"));
        assertEquals("spring:java:javascript", ids(service.getAllTopics()));
        assertEquals("Java 21", service.getTopicWithId("java").getSubjectName());
    }

    @Test
    public void deleteRemovesMatchingTopic() {
        service.deleteTopic("spring");
        assertEquals("java:javascript", ids(service.getAllTopics()));
    }

    @Test
    public void filterMinimumLengthIsStrictlyGreaterThan() {
        assertEquals("spring:javascript", ids(service.filterMinimumLengthForId(4)));
        assertEquals("javascript", ids(service.filterMinimumLengthForId(6)));
    }

    @Test
    public void stringHelpers() {
        String join = service.returnAllTopicIDWithStringSlicing();
        assertEquals("spring:java:javascript", join);
        assertEquals(":acgijnprstv", service.makeDistinctAndSortCharacters(join));
        assertEquals("java:javascript", service.splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoin(join));
        assertEquals("[spring]", service.findIdHavingCharacter());
    }

    @Test
    public void getTopicWithUnknownIdThrows() {
        assertThrows(java.util.NoSuchElementException.class, () ->
            service.getTopicWithId("missing"));
    }

    private static String ids(List<Topic> topics) {
        return topics.stream().map(Topic::getId).collect(Collectors.joining(":"));
    }
}
