package hello.service;

import hello.model.Topic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TopicServiceTests {

    private TopicService topicService;

    @BeforeEach
    void setUp() {
        topicService = new TopicService();
    }

    @Test
    void joinsAllTopicIdsWithColon() {
        assertThat(topicService.returnAllTopicIDWithStringSlicing()).isEqualTo("spring:java:javascript");
    }

    @Test
    void makesCharactersDistinctAndSorted() {
        assertThat(topicService.makeDistinctAndSortCharacters("spring:java:javascript"))
                .isEqualTo(":acgijnprstv");
    }

    @Test
    void keepsOnlyJavaIdsSortedAndJoined() {
        assertThat(topicService.splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoin("spring:java:javascript"))
                .isEqualTo("java:javascript");
    }

    @Test
    void findsIdsContainingTheLetterG() {
        assertThat(topicService.findIdHavingCharacter()).isEqualTo("[spring]");
    }

    @Test
    void filtersTopicsByMinimumIdLength() {
        assertThat(topicService.filterMinimumLengthForId(5))
                .extracting(Topic::getId)
                .containsExactly("spring", "javascript");
    }

    @Test
    void sortsTopicsById() {
        assertThat(topicService.sortTopicsWithID())
                .extracting(Topic::getId)
                .containsExactly("java", "javascript", "spring");
    }

    @Test
    void addsAndDeletesTopics() {
        topicService.addTopic(new Topic("kotlin", "Kotlin", "Kotlin Description"));
        assertThat(topicService.getTopicWithId("kotlin").getSubjectName()).isEqualTo("Kotlin");

        topicService.deleteTopic("kotlin");
        assertThat(topicService.getAllTopics()).extracting(Topic::getId).doesNotContain("kotlin");
    }

    /**
     * Documents current behaviour: an unknown id makes {@code Optional#get()} throw.
     */
    @Test
    void unknownIdCurrentlyThrows() {
        assertThatThrownBy(() -> topicService.getTopicWithId("does-not-exist"))
                .isInstanceOf(NoSuchElementException.class);
    }

    /**
     * Documents current behaviour: the backing list is returned directly, so callers can mutate it.
     */
    @Test
    void exposesTheBackingListDirectly() {
        List<Topic> topics = topicService.getAllTopics();
        topics.add(new Topic("leaked", "Leaked", "Leaked Description"));

        assertThat(topicService.getAllTopics()).hasSize(4);
    }

    /**
     * Documents current behaviour: updating an unknown id is a silent no-op.
     */
    @Test
    void updateOfUnknownIdCurrentlyNoOps() {
        topicService.updateTopic("does-not-exist", new Topic("ghost", "Ghost", "Ghost"));

        assertThat(topicService.getAllTopics()).hasSize(3);
    }
}
