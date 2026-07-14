package hello.service;

import hello.model.Topic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TopicServiceTest {

    private TopicService service;

    @BeforeEach
    void setUp() {
        service = new TopicService();
    }

    private List<String> ids(List<Topic> topics) {
        return topics.stream().map(Topic::getId).collect(Collectors.toList());
    }

    @Nested
    @DisplayName("CRUD list operations")
    class CrudOperations {

        @Test
        void getAllTopicsReturnsSeededTopics() {
            List<Topic> all = service.getAllTopics();
            assertEquals(3, all.size());
            assertEquals(Arrays.asList("spring", "java", "javascript"), ids(all));
        }

        @Test
        void getTopicWithIdReturnsMatchingTopic() {
            Topic topic = service.getTopicWithId("java");
            assertEquals("java", topic.getId());
            assertEquals("Core Java", topic.getSubjectName());
        }

        @Test
        void getTopicWithIdThrowsWhenMissing() {
            assertThrows(NoSuchElementException.class, () -> service.getTopicWithId("missing"));
        }

        @Test
        void addTopicAppendsToList() {
            service.addTopic(new Topic("go", "Go Lang", "Go Description"));
            assertEquals(4, service.getAllTopics().size());
            assertEquals("go", service.getTopicWithId("go").getId());
        }

        @Test
        void updateTopicReplacesExistingEntry() {
            service.updateTopic("java", new Topic("java", "Modern Java", "Updated"));
            assertEquals("Modern Java", service.getTopicWithId("java").getSubjectName());
            assertEquals(3, service.getAllTopics().size());
        }

        @Test
        void updateTopicIsNoOpWhenIdNotFound() {
            service.updateTopic("missing", new Topic("missing", "Nope", "Nope"));
            assertEquals(3, service.getAllTopics().size());
            assertEquals(Arrays.asList("spring", "java", "javascript"), ids(service.getAllTopics()));
        }

        @Test
        void deleteTopicRemovesMatchingEntry() {
            service.deleteTopic("java");
            assertEquals(2, service.getAllTopics().size());
            assertFalse(ids(service.getAllTopics()).contains("java"));
        }
    }

    @Nested
    @DisplayName("Filtering and sorting")
    class FilteringAndSorting {

        @Test
        void filterMinimumLengthForIdKeepsLongerIds() {
            List<Topic> filtered = service.filterMinimumLengthForId(6);
            assertEquals(Arrays.asList("javascript"), ids(filtered));
        }

        @Test
        void filterMinimumLengthForIdReturnsEmptyWhenNoneQualify() {
            assertTrue(service.filterMinimumLengthForId(100).isEmpty());
        }

        @Test
        void sortTopicsWithIdSortsAlphabetically() {
            List<Topic> sorted = service.sortTopicsWithID();
            assertEquals(Arrays.asList("java", "javascript", "spring"), ids(sorted));
        }
    }

    @Nested
    @DisplayName("String / stream transforms")
    class StringOperations {

        @Test
        void returnAllTopicIdWithStringSlicingJoinsWithColon() {
            assertEquals("spring:java:javascript", service.returnAllTopicIDWithStringSlicing());
        }

        @Test
        void makeDistinctAndSortCharactersRemovesDuplicatesAndSorts() {
            assertEquals("abcr", service.makeDistinctAndSortCharacters("cabbra"));
        }

        @Test
        void makeDistinctAndSortCharactersHandlesColonsAndLetters() {
            assertEquals(":acgijnprstv", service.makeDistinctAndSortCharacters("spring:java:javascript"));
        }

        @Test
        void makeDistinctAndSortCharactersEmptyInput() {
            assertEquals("", service.makeDistinctAndSortCharacters(""));
        }

        @Test
        void splitAllIdSelectsOnlyEntriesContainingJavaSortedAndJoined() {
            String result = service.splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoin(
                    "spring:java:javascript");
            assertEquals("java:javascript", result);
        }

        @Test
        void splitAllIdReturnsEmptyWhenNoJavaKeyword() {
            assertEquals("", service.splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoin("spring:go:rust"));
        }

        @Test
        void findIdHavingCharacterReturnsIdsContainingLetterG() {
            assertEquals("[spring]", service.findIdHavingCharacter());
        }
    }

    @Nested
    @DisplayName("File / NIO operations")
    class FileOperations {

        @Test
        void findAllFilesInPathAndSortListsProjectFiles() {
            String result = service.findAllFilesInPathAndSort();
            assertNotNull(result);
            assertFalse(result.startsWith(" Error"));
            assertTrue(result.contains("src"), "expected listing to contain project sources: " + result);
        }

        @Test
        void findParticularFileInPathAndSortFindsGradleFiles() {
            String result = service.findParticularFileInPathAndSort();
            assertNotNull(result);
            assertFalse(result.contains("IO exception"));
            assertTrue(result.contains("grad"), "expected to find gradle files: " + result);
        }

        @Test
        void findParticularFileInPathAndSortWithWalkFunctionFindsGradleFiles() {
            String result = service.findParticularFileInPathAndSortWithWalkFunction();
            assertNotNull(result);
            assertFalse(result.contains("IO exception"));
            assertTrue(result.contains("grad"), "expected to find gradle files: " + result);
        }

        @Test
        void readFileWithStreamFunctionExtractsPrintLines() {
            assertEquals(" Hello, this, is, Rehman", service.readFileWithStreamFunction());
        }
    }
}
