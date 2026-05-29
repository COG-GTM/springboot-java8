package hello.service;

import static org.junit.Assert.*;

import hello.model.Topic;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

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
    public void testAddTopic() {
        Topic topic = new Topic("python", "Python", "Python Description");
        topicService.addTopic(topic);
        List<Topic> topics = topicService.getAllTopics();
        assertEquals(4, topics.size());
    }

    @Test
    public void testUpdateTopic() {
        Topic updatedTopic = new Topic("java", "Java Updated", "Updated Desc");
        topicService.updateTopic("java", updatedTopic);
        Topic result = topicService.getTopicWithId("java");
        assertEquals("Java Updated", result.getSubjectName());
    }

    @Test
    public void testDeleteTopic() {
        topicService.deleteTopic("java");
        List<Topic> topics = topicService.getAllTopics();
        assertEquals(2, topics.size());
    }

    @Test
    public void testFilterMinimumLengthForId() {
        List<Topic> filtered = topicService.filterMinimumLengthForId(5);
        assertNotNull(filtered);
        for (Topic t : filtered) {
            assertTrue(t.getId().length() > 5);
        }
    }

    @Test
    public void testSortTopicsWithID() {
        List<Topic> sorted = topicService.sortTopicsWithID();
        assertNotNull(sorted);
        for (int i = 0; i < sorted.size() - 1; i++) {
            assertTrue(sorted.get(i).getId().compareTo(sorted.get(i + 1).getId()) <= 0);
        }
    }

    @Test
    public void testReturnAllTopicIDWithStringSlicing() {
        String result = topicService.returnAllTopicIDWithStringSlicing();
        assertNotNull(result);
        assertTrue(result.contains(":"));
        assertTrue(result.contains("java"));
    }

    @Test
    public void testMakeDistinctAndSortCharacters() {
        String input = "hello:world";
        String result = topicService.makeDistinctAndSortCharacters(input);
        assertNotNull(result);
        // All chars should be unique and sorted
        for (int i = 0; i < result.length() - 1; i++) {
            assertTrue(result.charAt(i) <= result.charAt(i + 1));
        }
    }

    @Test
    public void testSplitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoin() {
        String input = "java:spring:javascript";
        String result = topicService.splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoin(input);
        assertNotNull(result);
        assertTrue(result.contains("java"));
        assertTrue(result.contains("javascript"));
        assertFalse(result.contains("spring"));
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
