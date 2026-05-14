package hello.service;

import hello.model.Topic;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class TopicServiceTest {

    private TopicService topicService;

    @Before
    public void setUp() {
        topicService = new TopicService();
    }

    @Test
    public void testGetAllTopics() {
        List<Topic> topics = topicService.getAllTopics();
        assertNotNull(topics);
        assertEquals(3, topics.size());
    }

    @Test
    public void testGetTopicWithId() {
        Topic topic = topicService.getTopicWithId("java");
        assertNotNull(topic);
        assertEquals("java", topic.getId());
        assertEquals("Core Java", topic.getSubjectName());
    }

    @Test
    public void testGetTopicWithIdSpring() {
        Topic topic = topicService.getTopicWithId("spring");
        assertNotNull(topic);
        assertEquals("spring", topic.getId());
    }

    @Test
    public void testAddTopic() {
        Topic newTopic = new Topic("python", "Python", "Python Description");
        topicService.addTopic(newTopic);
        List<Topic> topics = topicService.getAllTopics();
        assertEquals(4, topics.size());
        assertEquals("python", topicService.getTopicWithId("python").getId());
    }

    @Test
    public void testUpdateTopicExisting() {
        Topic updatedTopic = new Topic("java", "Updated Java", "Updated Description");
        topicService.updateTopic("java", updatedTopic);
        Topic result = topicService.getTopicWithId("java");
        assertEquals("Updated Java", result.getSubjectName());
    }

    @Test
    public void testUpdateTopicNonExisting() {
        int sizeBefore = topicService.getAllTopics().size();
        Topic updatedTopic = new Topic("nonexistent", "No", "No");
        topicService.updateTopic("nonexistent", updatedTopic);
        assertEquals(sizeBefore, topicService.getAllTopics().size());
    }

    @Test
    public void testDeleteTopic() {
        topicService.deleteTopic("java");
        List<Topic> topics = topicService.getAllTopics();
        assertEquals(2, topics.size());
        assertTrue(topics.stream().noneMatch(t -> t.getId().equals("java")));
    }

    @Test
    public void testDeleteTopicNonExisting() {
        int sizeBefore = topicService.getAllTopics().size();
        topicService.deleteTopic("nonexistent");
        assertEquals(sizeBefore, topicService.getAllTopics().size());
    }

    @Test
    public void testFilterMinimumLengthForId() {
        List<Topic> filtered = topicService.filterMinimumLengthForId(4);
        assertNotNull(filtered);
        assertTrue(filtered.size() > 0);
        filtered.forEach(t -> assertTrue(t.getId().length() > 4));
    }

    @Test
    public void testFilterMinimumLengthForIdNoResults() {
        List<Topic> filtered = topicService.filterMinimumLengthForId(100);
        assertNotNull(filtered);
        assertEquals(0, filtered.size());
    }

    @Test
    public void testSortTopicsWithID() {
        List<Topic> sorted = topicService.sortTopicsWithID();
        assertNotNull(sorted);
        assertEquals(3, sorted.size());
        assertEquals("java", sorted.get(0).getId());
        assertEquals("javascript", sorted.get(1).getId());
        assertEquals("spring", sorted.get(2).getId());
    }

    @Test
    public void testReturnAllTopicIDWithStringSlicing() {
        String result = topicService.returnAllTopicIDWithStringSlicing();
        assertNotNull(result);
        assertTrue(result.contains("spring"));
        assertTrue(result.contains("java"));
        assertTrue(result.contains(":"));
    }

    @Test
    public void testMakeDistinctAndSortCharacters() {
        String result = topicService.makeDistinctAndSortCharacters("hello:world");
        assertNotNull(result);
        assertFalse(result.isEmpty());
        // Should have distinct characters sorted
        for (int i = 1; i < result.length(); i++) {
            assertTrue(result.charAt(i - 1) <= result.charAt(i));
        }
    }

    @Test
    public void testSplitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoin() {
        String input = "spring:java:javascript:python";
        String result = topicService.splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoin(input);
        assertNotNull(result);
        assertTrue(result.contains("java"));
        assertTrue(result.contains("javascript"));
        assertFalse(result.contains("spring"));
        assertFalse(result.contains("python"));
    }

    @Test
    public void testSplitAllIdWithNoJavaKeyword() {
        String input = "spring:python:ruby";
        String result = topicService.splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoin(input);
        assertNotNull(result);
        assertEquals("", result);
    }

    @Test
    public void testFindIdHavingCharacter() {
        String result = topicService.findIdHavingCharacter();
        assertNotNull(result);
        assertTrue(result.contains("spring"));
    }

    @Test
    public void testFindAllFilesInPathAndSort() {
        String result = topicService.findAllFilesInPathAndSort();
        assertNotNull(result);
    }

    @Test
    public void testFindParticularFileInPathAndSort() {
        String result = topicService.findParticularFileInPathAndSort();
        assertNotNull(result);
    }

    @Test
    public void testFindParticularFileInPathAndSortWithWalkFunction() {
        String result = topicService.findParticularFileInPathAndSortWithWalkFunction();
        assertNotNull(result);
    }

    @Test
    public void testReadFileWithStreamFunction() {
        String result = topicService.readFileWithStreamFunction();
        assertNotNull(result);
    }
}
