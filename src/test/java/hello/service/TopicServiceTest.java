package hello.service;

import java.util.List;

import hello.model.Topic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TopicServiceTest {

    private TopicService topicService;

    @BeforeEach
    void setUp() {
        topicService = new TopicService();
    }

    @Test
    void returnsSeededTopics() {
        assertThat(topicService.getAllTopics())
                .extracting(Topic::getId)
                .containsExactly("spring", "java", "javascript");
    }

    @Test
    void addsUpdatesAndDeletesTopics() {
        topicService.addTopic(new Topic("kotlin", "Kotlin", "Kotlin Description"));
        assertThat(topicService.getTopicWithId("kotlin").getSubjectName()).isEqualTo("Kotlin");

        topicService.updateTopic("kotlin", new Topic("kotlin", "Kotlin 2", "Updated"));
        assertThat(topicService.getTopicWithId("kotlin").getSubjectName()).isEqualTo("Kotlin 2");

        topicService.deleteTopic("kotlin");
        assertThat(topicService.getAllTopics()).hasSize(3);
    }

    @Test
    void filtersTopicsByMinimumIdLength() {
        List<Topic> filtered = topicService.filterMinimumLengthForId(5);
        assertThat(filtered).extracting(Topic::getId).containsExactly("spring", "javascript");
    }

    @Test
    void appliesStringOperationsOverTopicIds() {
        String joined = topicService.returnAllTopicIDWithStringSlicing();
        assertThat(joined).isEqualTo("spring:java:javascript");
        assertThat(topicService.makeDistinctAndSortCharacters(joined)).isEqualTo(":acgijnprstv");
        assertThat(topicService.splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoin(joined))
                .isEqualTo("java:javascript");
        assertThat(topicService.findIdHavingCharacter()).isEqualTo("[spring]");
    }

    @Test
    void sortsTopicsById() {
        assertThat(topicService.sortTopicsWithID())
                .extracting(Topic::getId)
                .containsExactly("java", "javascript", "spring");
    }
}
