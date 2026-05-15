package hello.service;

import hello.model.Topic;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.Assert.*;

public class TopicServiceTest {

    private TopicService topicService;

    @Before
    public void setUp() {
        topicService = new TopicService();
    }

    @Test
    public void getAllTopics_returnsThreeDefaultTopics() {
        List<Topic> topics = topicService.getAllTopics();
        assertEquals(3, topics.size());
        assertEquals("spring", topics.get(0).getId());
        assertEquals("java", topics.get(1).getId());
        assertEquals("javascript", topics.get(2).getId());
    }

    @Test
    public void getTopicWithId_existingId_returnsTopic() {
        Topic topic = topicService.getTopicWithId("spring");
        assertNotNull(topic);
        assertEquals("spring", topic.getId());
        assertEquals("Spring Framework", topic.getSubjectName());
        assertEquals("Spring Framework Description", topic.getSubjectDescription());
    }

    @Test(expected = NoSuchElementException.class)
    public void getTopicWithId_nonExistentId_throwsNoSuchElementException() {
        topicService.getTopicWithId("nonexistent");
    }

    @Test
    public void addTopic_addsNewTopicToList() {
        Topic newTopic = new Topic("python", "Python Language", "Python Description");
        int sizeBefore = topicService.getAllTopics().size();
        topicService.addTopic(newTopic);
        List<Topic> topics = topicService.getAllTopics();
        assertEquals(sizeBefore + 1, topics.size());
        assertEquals("python", topics.get(topics.size() - 1).getId());
    }

    @Test
    public void updateTopic_existingId_updatesTopic() {
        Topic updatedTopic = new Topic("spring", "Spring Boot", "Updated Description");
        topicService.updateTopic("spring", updatedTopic);
        Topic result = topicService.getTopicWithId("spring");
        assertEquals("Spring Boot", result.getSubjectName());
        assertEquals("Updated Description", result.getSubjectDescription());
    }

    @Test
    public void updateTopic_nonExistentId_noOp() {
        int sizeBefore = topicService.getAllTopics().size();
        Topic updatedTopic = new Topic("nonexistent", "Name", "Desc");
        topicService.updateTopic("nonexistent", updatedTopic);
        assertEquals(sizeBefore, topicService.getAllTopics().size());
    }

    @Test
    public void deleteTopic_existingId_removesTopic() {
        topicService.deleteTopic("java");
        List<Topic> topics = topicService.getAllTopics();
        assertEquals(2, topics.size());
        assertTrue(topics.stream().noneMatch(t -> t.getId().equals("java")));
    }

    @Test
    public void deleteTopic_nonExistentId_noOp() {
        int sizeBefore = topicService.getAllTopics().size();
        topicService.deleteTopic("nonexistent");
        assertEquals(sizeBefore, topicService.getAllTopics().size());
    }

    @Test
    public void filterMinimumLengthForId_four_returnsSpringAndJavascript() {
        List<Topic> filtered = topicService.filterMinimumLengthForId(4);
        assertEquals(2, filtered.size());
        assertTrue(filtered.stream().anyMatch(t -> t.getId().equals("spring")));
        assertTrue(filtered.stream().anyMatch(t -> t.getId().equals("javascript")));
    }

    @Test
    public void filterMinimumLengthForId_hundred_returnsEmpty() {
        List<Topic> filtered = topicService.filterMinimumLengthForId(100);
        assertTrue(filtered.isEmpty());
    }

    @Test
    public void sortTopicsWithID_returnsSortedByIdAlphabetically() {
        List<Topic> sorted = topicService.sortTopicsWithID();
        assertEquals("java", sorted.get(0).getId());
        assertEquals("javascript", sorted.get(1).getId());
        assertEquals("spring", sorted.get(2).getId());
    }

    @Test
    public void returnAllTopicIDWithStringSlicing_returnsColonJoinedIds() {
        String result = topicService.returnAllTopicIDWithStringSlicing();
        assertEquals("spring:java:javascript", result);
    }

    @Test
    public void makeDistinctAndSortCharacters_returnsSortedDistinctChars() {
        String result = topicService.makeDistinctAndSortCharacters("abc:abc");
        String expected = ":abc";
        assertEquals(expected, result);
    }

    @Test
    public void splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoin_returnsJavaAndJavascript() {
        String result = topicService.splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoin("spring:java:javascript");
        assertEquals("java:javascript", result);
    }

    @Test
    public void findIdHavingCharacter_returnsIdsContainingG() {
        String result = topicService.findIdHavingCharacter();
        assertTrue(result.contains("spring"));
        assertFalse(result.contains("java"));
    }

    @Test
    public void findAllFilesInPathAndSort_returnsNonNullString() {
        String result = topicService.findAllFilesInPathAndSort();
        assertNotNull(result);
    }

    @Test
    public void findParticularFileInPathAndSort_returnsNonNullString() {
        String result = topicService.findParticularFileInPathAndSort();
        assertNotNull(result);
    }

    @Test
    public void findParticularFileInPathAndSortWithWalkFunction_returnsNonNullString() {
        String result = topicService.findParticularFileInPathAndSortWithWalkFunction();
        assertNotNull(result);
    }

    @Test
    public void readFileWithStreamFunction_returnsResultOrIOException() {
        String result = topicService.readFileWithStreamFunction();
        assertNotNull(result);
    }
}
