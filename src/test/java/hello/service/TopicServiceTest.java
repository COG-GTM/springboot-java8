package hello.service;

import hello.model.Topic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TopicServiceTest {

    TopicService topicService;

    @BeforeEach
    void setUp() {
        topicService = new TopicService();
    }

    @Test
    void getAllTopics_containsSeedData() {
        List<Topic> all = topicService.getAllTopics();
        assertThat(all).hasSize(3);
        assertThat(all).extracting(Topic::id).containsExactlyInAnyOrder("spring", "java", "javascript");
    }

    @Test
    void getTopicWithId_returnsEmptyOptionalWhenMissing() {
        assertThat(topicService.getTopicWithId("does-not-exist")).isEmpty();
    }

    @Test
    void addAndDeleteTopic_roundTrip() {
        Topic topic = new Topic("kotlin", "Kotlin", "desc");
        topicService.addTopic(topic);
        assertThat(topicService.getTopicWithId("kotlin")).contains(topic);

        topicService.deleteTopic("kotlin");
        assertThat(topicService.getTopicWithId("kotlin")).isEmpty();
    }

    @Test
    void updateTopic_replacesExistingEntry() {
        Topic updated = new Topic("java", "Modern Java", "updated");
        topicService.updateTopic("java", updated);
        assertThat(topicService.getTopicWithId("java")).contains(updated);
    }

    @Test
    void filterMinimumLengthForId_filtersByIdLength() {
        assertThat(topicService.filterMinimumLengthForId(6))
                .extracting(Topic::id).containsExactlyInAnyOrder("javascript");
        assertThat(topicService.filterMinimumLengthForId(5))
                .extracting(Topic::id).containsExactlyInAnyOrder("spring", "javascript");
    }

    @Test
    void sortTopicsWithID_sortsLexicographically() {
        List<Topic> sorted = topicService.sortTopicsWithID();
        assertThat(sorted).extracting(Topic::id).containsExactly("java", "javascript", "spring");
    }

    @Test
    void returnAllTopicIDWithStringSlicing_joinsWithColon() {
        assertThat(topicService.returnAllTopicIDWithStringSlicing())
                .contains("spring").contains("java").contains("javascript");
    }

    @Test
    void findIdHavingCharacter_returnsIdsContainingG() {
        String result = topicService.findIdHavingCharacter();
        assertThat(result).contains("spring");
    }

    @Test
    void splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoin_filtersJavaIds() {
        String joined = topicService.returnAllTopicIDWithStringSlicing();
        String result = topicService.splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoin(joined);
        assertThat(result).contains("java");
        assertThat(result).doesNotContain("spring");
    }

    @Test
    void makeDistinctAndSortCharacters_returnsDistinctSortedChars() {
        String result = topicService.makeDistinctAndSortCharacters("ccbbaa");
        assertThat(result).isEqualTo("abc");
    }

    @Test
    void getAllTopicsList_isImmutableCopy() {
        List<Topic> all = topicService.getAllTopics();
        Optional<Topic> firstBefore = topicService.getTopicWithId("spring");
        assertThat(firstBefore).isPresent();
        assertThat(all).isUnmodifiable();
    }
}
