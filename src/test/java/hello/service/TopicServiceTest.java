package hello.service;

import hello.exception.TopicNotFoundException;
import hello.model.Topic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class TopicServiceTest {

    private TopicService topicService;

    @BeforeEach
    void setUp() {
        topicService = new TopicService();
    }

    @Test
    void getAllTopicsReturnsInitialThreeTopics() {
        List<Topic> topics = topicService.getAllTopics();
        assertNotNull(topics);
        assertEquals(3, topics.size());
    }

    @Test
    void getTopicWithValidIdReturnsMatchingTopic() {
        Topic topic = topicService.getTopicWithId("java");
        assertNotNull(topic);
        assertEquals("java", topic.getId());
        assertEquals("Core Java", topic.getSubjectName());
    }

    @Test
    void getTopicWithInvalidIdThrowsTopicNotFoundException() {
        TopicNotFoundException ex = assertThrows(
                TopicNotFoundException.class,
                () -> topicService.getTopicWithId("does-not-exist")
        );
        assertTrue(ex.getMessage().contains("does-not-exist"));
    }

    @Test
    void addTopicAppendsToList() {
        Topic newTopic = new Topic("python", "Python Language", "Python Description");
        topicService.addTopic(newTopic);

        assertEquals(4, topicService.getAllTopics().size());
        assertEquals(newTopic, topicService.getTopicWithId("python"));
    }

    @Test
    void updateTopicReplacesExistingTopic() {
        Topic updated = new Topic("java", "Java 17", "Updated Java Description");
        topicService.updateTopic("java", updated);

        Topic result = topicService.getTopicWithId("java");
        assertEquals("Java 17", result.getSubjectName());
        assertEquals("Updated Java Description", result.getSubjectDescription());
    }

    @Test
    void deleteTopicRemovesFromList() {
        topicService.deleteTopic("spring");

        assertEquals(2, topicService.getAllTopics().size());
        assertThrows(TopicNotFoundException.class,
                () -> topicService.getTopicWithId("spring"));
    }

    @Test
    void sortTopicsWithIDReturnsSortedListWithoutMutatingOriginal() {
        List<Topic> originalOrder = topicService.getAllTopics();
        List<String> originalIds = originalOrder.stream().map(Topic::getId).collect(java.util.stream.Collectors.toList());

        List<Topic> sorted = topicService.sortTopicsWithID();
        List<String> sortedIds = sorted.stream().map(Topic::getId).collect(java.util.stream.Collectors.toList());

        assertEquals(java.util.Arrays.asList("java", "javascript", "spring"), sortedIds);
        assertNotSame(originalOrder, sorted);
        assertEquals(originalIds, originalOrder.stream().map(Topic::getId).collect(java.util.stream.Collectors.toList()),
                "Original list order should not be mutated");
    }

    @Test
    void filterMinimumLengthForIdFiltersById() {
        List<Topic> filtered = topicService.filterMinimumLengthForId(5);
        assertEquals(2, filtered.size());
        assertTrue(filtered.stream().allMatch(t -> t.getId().length() > 5));
    }

    @Test
    void stringOperationMethodsRunWithoutError() {
        assertDoesNotThrow(() -> {
            String join = topicService.returnAllTopicIDWithStringSlicing();
            assertNotNull(join);
            assertFalse(join.isEmpty());

            assertNotNull(topicService.makeDistinctAndSortCharacters(join));
            assertNotNull(topicService.splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoin(join));
            assertNotNull(topicService.findIdHavingCharacter());
        });
    }
}
