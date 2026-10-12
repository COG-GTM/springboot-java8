package hello.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import hello.model.Topic;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class TopicServiceTest {

    private TopicService topicService;

    @BeforeEach
    void setUp() {
        topicService = new TopicService();
    }

    @Test
    void getAllTopicsReturnsSeedData() {
        assertThat(topicService.getAllTopics())
                .extracting(Topic::getId)
                .containsExactly("spring", "java", "javascript");
    }

    @Test
    void getTopicWithIdReturnsMatchingTopic() {
        Topic topic = topicService.getTopicWithId("java");
        assertThat(topic.getId()).isEqualTo("java");
        assertThat(topic.getSubjectName()).isEqualTo("Core Java");
        assertThat(topic.getSubjectDescription()).isEqualTo("Java Description");
    }

    @Test
    void getTopicWithIdThrowsNotFoundForUnknownId() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> topicService.getTopicWithId("unknown"));
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getReason()).isEqualTo("Topic not found: unknown");
    }

    @Test
    void addTopicAppendsToList() {
        topicService.addTopic(new Topic("kotlin", "Kotlin", "Kotlin Description"));

        assertThat(topicService.getAllTopics()).hasSize(4);
        assertThat(topicService.getTopicWithId("kotlin").getSubjectName()).isEqualTo("Kotlin");
    }

    @Test
    void updateTopicReplacesExistingTopic() {
        topicService.updateTopic("java", new Topic("java", "Modern Java", "Java 17 Description"));

        assertThat(topicService.getAllTopics()).hasSize(3);
        Topic updated = topicService.getTopicWithId("java");
        assertThat(updated.getSubjectName()).isEqualTo("Modern Java");
        assertThat(updated.getSubjectDescription()).isEqualTo("Java 17 Description");
    }

    @Test
    void updateTopicWithUnknownIdIsNoOp() {
        topicService.updateTopic("unknown", new Topic("unknown", "X", "Y"));

        assertThat(topicService.getAllTopics())
                .extracting(Topic::getId)
                .containsExactly("spring", "java", "javascript");
    }

    @Test
    void deleteTopicRemovesTopic() {
        topicService.deleteTopic("spring");

        assertThat(topicService.getAllTopics())
                .extracting(Topic::getId)
                .containsExactly("java", "javascript");
        assertThrows(ResponseStatusException.class, () -> topicService.getTopicWithId("spring"));
    }

    @Test
    void filterMinimumLengthForIdReturnsIdsLongerThanMinimum() {
        List<Topic> result = topicService.filterMinimumLengthForId(5);

        assertThat(result).extracting(Topic::getId).containsExactly("spring", "javascript");
    }

    @Test
    void sortTopicsWithIdSortsAlphabetically() {
        assertThat(topicService.sortTopicsWithID())
                .extracting(Topic::getId)
                .containsExactly("java", "javascript", "spring");
    }
}
