package hello.service;

import hello.model.Topic;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TopicServiceTest {

    @Test
    public void getsAllSeededTopics() {
        TopicService service = new TopicService();

        assertEquals(3, service.getAllTopics().size());
        assertEquals("spring", service.getAllTopics().get(0).getId());
        assertEquals("java", service.getAllTopics().get(1).getId());
        assertEquals("javascript", service.getAllTopics().get(2).getId());
    }

    @Test
    public void getsTopicById() {
        TopicService service = new TopicService();

        assertEquals("Core Java", service.getTopicWithId("java").getSubjectName());
    }

    @Test(expected = NoSuchElementException.class)
    public void missingTopicThrows() {
        new TopicService().getTopicWithId("missing");
    }

    @Test
    public void addsTopic() {
        TopicService service = new TopicService();
        Topic topic = new Topic("kotlin", "Kotlin", "Kotlin Description");

        service.addTopic(topic);

        assertEquals(4, service.getAllTopics().size());
        assertEquals(topic, service.getAllTopics().get(3));
    }

    @Test
    public void updatesExistingTopicInPlace() {
        TopicService service = new TopicService();
        Topic replacement = new Topic("java", "Updated Java", "Updated Description");

        service.updateTopic("java", replacement);

        assertEquals(replacement, service.getAllTopics().get(1));
    }

    @Test
    public void updatingUnknownTopicDoesNothing() {
        TopicService service = new TopicService();
        List<Topic> topics = service.getAllTopics();
        Topic replacement = new Topic("missing", "Missing", "Missing");

        service.updateTopic("missing", replacement);

        assertEquals(3, topics.size());
        assertEquals("spring", topics.get(0).getId());
        assertEquals("javascript", topics.get(2).getId());
    }

    @Test
    public void deletesMatchingTopicAndIgnoresUnknownId() {
        TopicService service = new TopicService();

        service.deleteTopic("java");
        assertEquals(2, service.getAllTopics().size());
        assertFalse(service.getAllTopics().stream().anyMatch(topic -> "java".equals(topic.getId())));

        service.deleteTopic("missing");
        assertEquals(2, service.getAllTopics().size());
    }

    @Test
    public void filtersIdsStrictlyLongerThanMinimum() {
        TopicService service = new TopicService();

        assertEquals(2, service.filterMinimumLengthForId(4).size());
        assertEquals("spring", service.filterMinimumLengthForId(4).get(0).getId());
        assertEquals("javascript", service.filterMinimumLengthForId(4).get(1).getId());
        assertEquals("javascript", service.filterMinimumLengthForId(6).get(0).getId());
    }

    @Test
    public void sortsTopicsById() {
        TopicService service = new TopicService();

        List<Topic> sorted = service.sortTopicsWithID();

        assertEquals("java", sorted.get(0).getId());
        assertEquals("javascript", sorted.get(1).getId());
        assertEquals("spring", sorted.get(2).getId());
    }

    @Test
    public void joinsTopicIds() {
        assertEquals("spring:java:javascript",
                new TopicService().returnAllTopicIDWithStringSlicing());
    }

    @Test
    public void makesCharactersDistinctAndSorted() {
        TopicService service = new TopicService();

        assertEquals(":acgijnprstv",
                service.makeDistinctAndSortCharacters("spring:java:javascript"));
    }

    @Test
    public void selectsJavaIdsSortsAndJoins() {
        assertEquals("java:javascript",
                new TopicService().splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoin(
                        "spring:java:javascript"));
    }

    @Test
    public void findsIdsContainingG() {
        assertEquals("[spring]", new TopicService().findIdHavingCharacter());
    }

    @Test
    public void listsVisibleFilesInSortedOrder() {
        String files = new TopicService().findAllFilesInPathAndSort();

        assertTrue(files.contains("pom.xml"));
        assertFalse(files.contains(".gitignore"));
    }

    @Test
    public void findsParticularFiles() {
        TopicService service = new TopicService();

        assertNotNull(service.findParticularFileInPathAndSort());
        String walkedFiles = service.findParticularFileInPathAndSortWithWalkFunction();
        assertNotNull(walkedFiles);
        assertTrue(walkedFiles.contains("grad"));
    }

    @Test
    public void readsPrintLinesFromTempFile() {
        assertEquals(" Hello, this, is, Rehman",
                new TopicService().readFileWithStreamFunction());
        assertTrue(Files.exists(Paths.get("temp.txt")));
    }
}
