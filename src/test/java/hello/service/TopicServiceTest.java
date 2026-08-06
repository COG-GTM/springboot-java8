package hello.service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import hello.model.Topic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link TopicService} keeps its topics in a mutable instance field, so as a Spring singleton its
 * state leaks between tests. These unit tests therefore build a brand new service in {@link BeforeEach}
 * instead of injecting the bean: every test starts from the three seeded topics and nothing has to be
 * undone afterwards.
 */
class TopicServiceTest {

    private TopicService topicService;

    @BeforeEach
    void createFreshService() {
        topicService = new TopicService();
    }

    /**
     * Single place where a {@link Topic} accessor is used, so the parallel conversion of the models to
     * records only has to touch one line in this class.
     */
    private static List<String> idsOf(List<Topic> topics) {
        return topics.stream().map(Topic::id).collect(Collectors.toList());
    }

    @Test
    void seedsThreeTopics() {
        assertThat(idsOf(topicService.getAllTopics()))
                .containsExactly("spring", "java", "javascript");
    }

    @Test
    void getTopicWithIdReturnsTheMatchingTopic() {
        Topic topic = topicService.getTopicWithId("java").orElseThrow();

        assertThat(idsOf(List.of(topic))).containsExactly("java");
        assertThat(topic.subjectName()).isEqualTo("Core Java");
    }

    @Test
    void getTopicWithIdReportsAMissingTopicWithoutThrowing() {
        assertThatCode(() -> topicService.getTopicWithId("does-not-exist")).doesNotThrowAnyException();
        assertThat(topicService.getTopicWithId("does-not-exist")).isEmpty();
    }

    @Test
    void addTopicAppendsToTheList() {
        topicService.addTopic(new Topic("kotlin", "Kotlin", "Kotlin Description"));

        assertThat(idsOf(topicService.getAllTopics())).containsExactly("spring", "java", "javascript", "kotlin");
    }

    @Test
    void updateTopicReplacesTheTopicInPlace() {
        topicService.updateTopic("java", new Topic("java", "Updated Java", "Updated Description"));

        List<Topic> topics = topicService.getAllTopics();
        assertThat(idsOf(topics)).containsExactly("spring", "java", "javascript");
        assertThat(topics.get(1).subjectName()).isEqualTo("Updated Java");
    }

    @Test
    void updateTopicIsANoOpForAnUnknownId() {
        topicService.updateTopic("kotlin", new Topic("kotlin", "Kotlin", "Kotlin Description"));

        assertThat(idsOf(topicService.getAllTopics())).containsExactly("spring", "java", "javascript");
    }

    @Test
    void deleteTopicRemovesOnlyTheMatchingTopic() {
        topicService.deleteTopic("java");

        assertThat(idsOf(topicService.getAllTopics())).containsExactly("spring", "javascript");
    }

    @Test
    void deleteTopicIsANoOpForAnUnknownId() {
        topicService.deleteTopic("kotlin");

        assertThat(idsOf(topicService.getAllTopics())).hasSize(3);
    }

    @Test
    void filterMinimumLengthForIdKeepsIdsStrictlyLongerThanTheGivenLength() {
        assertThat(idsOf(topicService.filterMinimumLengthForId(4)))
                .containsExactly("spring", "javascript");
        assertThat(idsOf(topicService.filterMinimumLengthForId(6)))
                .containsExactly("javascript");
        assertThat(topicService.filterMinimumLengthForId(20)).isEmpty();
    }

    @Test
    void sortTopicsWithIDSortsTheUnderlyingListById() {
        assertThat(idsOf(topicService.sortTopicsWithID()))
                .containsExactly("java", "javascript", "spring");

        // The sort mutates the service's own list, which is exactly why these tests use a fresh instance.
        assertThat(idsOf(topicService.getAllTopics()))
                .containsExactly("java", "javascript", "spring");
    }

    @Test
    void returnAllTopicIDWithStringSlicingJoinsIdsWithAColon() {
        assertThat(topicService.returnAllTopicIDWithStringSlicing()).isEqualTo("spring:java:javascript");
    }

    @Test
    void makeDistinctAndSortCharactersReturnsSortedDistinctCharacters() {
        assertThat(topicService.makeDistinctAndSortCharacters("spring:java:javascript"))
                .isEqualTo(":acgijnprstv");
        assertThat(topicService.makeDistinctAndSortCharacters("")).isEmpty();
    }

    @Test
    void splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoinKeepsOnlyJavaIds() {
        assertThat(topicService.splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoin("spring:java:javascript"))
                .isEqualTo("java:javascript");
        assertThat(topicService.splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoin("spring:kotlin"))
                .isEmpty();
    }

    @Test
    void findIdHavingCharacterMatchesIdsContainingG() {
        assertThat(topicService.findIdHavingCharacter()).isEqualTo("[spring]");
    }

    // The file operations below walk the process working directory (the module base directory when run
    // by Surefire) and read temp.txt from it. Their exact output changes whenever a file is added or
    // removed from the repository, so they are asserted loosely on purpose.

    @Test
    void findAllFilesInPathAndSortListsVisibleFilesInTheWorkingDirectory() {
        String listed = topicService.findAllFilesInPathAndSort();

        assertThat(listed).isNotNull().isNotEqualTo(" Error in IO");
        assertThat(listed).contains("pom.xml");
        assertThat(listed).doesNotContain(".git;");
    }

    @Test
    void findParticularFileInPathAndSortReturnsAStringWithoutFailing() {
        assertThat(topicService.findParticularFileInPathAndSort())
                .isNotNull()
                .isNotEqualTo(" IO exception ");
    }

    @Test
    void findParticularFileInPathAndSortWithWalkFunctionReturnsAStringWithoutFailing() {
        assertThat(topicService.findParticularFileInPathAndSortWithWalkFunction())
                .isNotNull()
                .isNotEqualTo(" IO exception ");
    }

    @Test
    void readFileWithStreamFunctionReturnsTheLinesStartingWithPrint() {
        Assumptions.assumeTrue(Files.exists(Paths.get("temp.txt")), "temp.txt is read relative to the working directory");

        String read = topicService.readFileWithStreamFunction();

        assertThat(read).isNotNull().isNotEqualTo(" IO exception ");
        assertThat(read).contains(" Hello");
        assertThat(read).doesNotContain("print");
    }
}
