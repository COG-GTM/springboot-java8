package hello.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;

import hello.model.Topic;

/**
 * Characterization tests for the deterministic, in-memory business logic in
 * {@link TopicService}. These document the current behavior of the seeded
 * topic list (ids "spring", "java", "javascript") rather than prescribing new
 * behavior.
 */
public class TopicServiceTest {

    private TopicService topicService;

    @Before
    public void setUp() {
        topicService = new TopicService();
    }

    @Test
    public void getAllTopicsReturnsSeededTopics() {
        List<Topic> topics = topicService.getAllTopics();

        assertEquals(3, topics.size());
        assertEquals("spring", topics.get(0).getId());
        assertEquals("java", topics.get(1).getId());
        assertEquals("javascript", topics.get(2).getId());
    }

    @Test
    public void getTopicWithIdReturnsMatchingTopic() {
        Topic topic = topicService.getTopicWithId("java");

        assertNotNull(topic);
        assertEquals("java", topic.getId());
        assertEquals("Core Java", topic.getSubjectName());
    }

    @Test(expected = NoSuchElementException.class)
    public void getTopicWithIdThrowsWhenNotFound() {
        // Documents the current no-fallback behavior of .findFirst().get().
        topicService.getTopicWithId("does-not-exist");
    }

    @Test
    public void addTopicGrowsListAndIsRetrievable() {
        topicService.addTopic(new Topic("go", "Go", "Go Description"));

        assertEquals(4, topicService.getAllTopics().size());
        Topic added = topicService.getTopicWithId("go");
        assertEquals("go", added.getId());
        assertEquals("Go", added.getSubjectName());
    }

    @Test
    public void updateTopicReplacesExistingTopic() {
        Topic replacement = new Topic("java", "Updated Java", "Updated Description");
        topicService.updateTopic("java", replacement);

        Topic updated = topicService.getTopicWithId("java");
        assertEquals("Updated Java", updated.getSubjectName());
        assertEquals("Updated Description", updated.getSubjectDescription());
        assertEquals(3, topicService.getAllTopics().size());
    }

    @Test
    public void updateTopicIsNoOpForUnknownId() {
        Topic replacement = new Topic("go", "Go", "Go Description");
        topicService.updateTopic("does-not-exist", replacement);

        List<Topic> topics = topicService.getAllTopics();
        assertEquals(3, topics.size());
        assertEquals("spring", topics.get(0).getId());
        assertEquals("java", topics.get(1).getId());
        assertEquals("javascript", topics.get(2).getId());
    }

    @Test
    public void deleteTopicRemovesMatchingTopic() {
        topicService.deleteTopic("java");

        List<Topic> topics = topicService.getAllTopics();
        assertEquals(2, topics.size());
        assertFalse(topics.stream().anyMatch(topic -> "java".equals(topic.getId())));
    }

    @Test
    public void deleteTopicIsNoOpForUnknownId() {
        topicService.deleteTopic("does-not-exist");

        assertEquals(3, topicService.getAllTopics().size());
    }

    @Test
    public void filterMinimumLengthForIdReturnsOnlyLongerIds() {
        List<Topic> filtered = topicService.filterMinimumLengthForId(4);

        // Strictly greater than 4: "spring" (6) and "javascript" (10), not "java" (4).
        assertEquals(2, filtered.size());
        assertEquals("spring", filtered.get(0).getId());
        assertEquals("javascript", filtered.get(1).getId());
    }

    @Test
    public void filterMinimumLengthForIdBoundaryValues() {
        // length > 6 keeps only "javascript" (10); "spring" (6) is excluded.
        List<Topic> aboveSix = topicService.filterMinimumLengthForId(6);
        assertEquals(1, aboveSix.size());
        assertEquals("javascript", aboveSix.get(0).getId());

        // length > 10 keeps nothing (longest id is 10).
        assertTrue(topicService.filterMinimumLengthForId(10).isEmpty());

        // length > 0 keeps all three.
        assertEquals(3, topicService.filterMinimumLengthForId(0).size());
    }

    @Test
    public void sortTopicsWithIDReturnsAscendingById() {
        List<Topic> sorted = topicService.sortTopicsWithID();

        assertEquals(3, sorted.size());
        assertEquals("java", sorted.get(0).getId());
        assertEquals("javascript", sorted.get(1).getId());
        assertEquals("spring", sorted.get(2).getId());
    }

    @Test
    public void returnAllTopicIDWithStringSlicingJoinsWithColon() {
        assertEquals("spring:java:javascript", topicService.returnAllTopicIDWithStringSlicing());
    }

    @Test
    public void makeDistinctAndSortCharactersReturnsDistinctSorted() {
        assertEquals("abc", topicService.makeDistinctAndSortCharacters("cba"));
        assertEquals("abn", topicService.makeDistinctAndSortCharacters("banana"));
    }

    @Test
    public void splitAllIdWithColonSelectsJavaEntriesSortedAndJoined() {
        String result = topicService.splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoin(
                "spring:java:javascript");

        // Only entries containing "java", sorted ascending, rejoined with ":".
        assertEquals("java:javascript", result);
    }

    @Test
    public void findIdHavingCharacterMatchesIdsContainingG() {
        String result = topicService.findIdHavingCharacter();

        // Regex .*g.* matches only "spring" among the seeded ids.
        assertTrue(result.contains("spring"));
        assertFalse(result.contains("javascript"));
        assertEquals("[spring]", result);
    }
}
