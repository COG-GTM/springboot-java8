package hello.service;

import hello.model.Topic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TopicServiceTest {

    private TopicService topicService;

    @BeforeEach
    public void setUp() {
        topicService = new TopicService();
    }

    @Test
    public void getAllTopicsReturnsThreeDefaults() {
        assertEquals(3, topicService.getAllTopics().size());
    }

    @Test
    public void getTopicWithIdReturnsMatchingTopic() {
        Topic topic = topicService.getTopicWithId("java");
        assertEquals("java", topic.getId());
        assertEquals("Core Java", topic.getSubjectName());
    }

    @Test
    public void addTopicIncreasesListSize() {
        topicService.addTopic(new Topic("python", "Python", "Python Description"));
        assertEquals(4, topicService.getAllTopics().size());
        assertEquals("Python", topicService.getTopicWithId("python").getSubjectName());
    }

    @Test
    public void updateTopicModifiesExistingEntry() {
        topicService.updateTopic("java", new Topic("java", "Updated Java", "Updated"));
        assertEquals("Updated Java", topicService.getTopicWithId("java").getSubjectName());
    }

    @Test
    public void deleteTopicRemovesEntry() {
        topicService.deleteTopic("java");
        assertEquals(2, topicService.getAllTopics().size());
        assertTrue(topicService.getAllTopics().stream().noneMatch(t -> t.getId().equals("java")));
    }

    @Test
    public void filterMinimumLengthForIdReturnsCorrectSubset() {
        List<Topic> result = topicService.filterMinimumLengthForId(4);
        assertEquals(2, result.size());
        assertEquals("spring", result.get(0).getId());
        assertEquals("javascript", result.get(1).getId());
    }

    @Test
    public void sortTopicsWithIDReturnsSortedList() {
        List<Topic> sorted = topicService.sortTopicsWithID();
        assertEquals("java", sorted.get(0).getId());
        assertEquals("javascript", sorted.get(1).getId());
        assertEquals("spring", sorted.get(2).getId());
    }

    @Test
    public void returnAllTopicIDWithStringSlicingJoinsIds() {
        assertEquals("spring:java:javascript", topicService.returnAllTopicIDWithStringSlicing());
    }

    @Test
    public void makeDistinctAndSortCharactersReturnsExpected() {
        String join = topicService.returnAllTopicIDWithStringSlicing();
        assertEquals(":acgijnprstv", topicService.makeDistinctAndSortCharacters(join));
    }

    @Test
    public void splitAllIdWithColonSelectsJavaKeywordSortedJoined() {
        String join = topicService.returnAllTopicIDWithStringSlicing();
        assertEquals("java:javascript",
                topicService.splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoin(join));
    }

    @Test
    public void findIdHavingCharacterReturnsIdsContainingG() {
        String result = topicService.findIdHavingCharacter();
        assertTrue(result.contains("spring"));
        assertFalse(result.contains("java"));
    }
}
