package hello.service;

import hello.model.Topic;
import hello.repository.TopicRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TopicServiceTest {

    private TopicRepository repository;
    private JdbcTemplate jdbcTemplate;
    private TopicService service;

    private final List<Topic> topics = Arrays.asList(
            new Topic("spring", "Spring Framework", "Spring Framework Description"),
            new Topic("java", "Core Java", "Java Description"),
            new Topic("javascript", "javascript Framework", "javascript Framework Description"));

    @BeforeEach
    void setUp() {
        repository = mock(TopicRepository.class);
        jdbcTemplate = mock(JdbcTemplate.class);
        when(repository.findAll()).thenReturn(topics);
        service = new TopicService(repository, jdbcTemplate);
    }

    @Test
    void getTopicWithIdReturnsEmptyOptionalWhenMissing() {
        when(repository.findById("nope")).thenReturn(Optional.empty());
        assertThat(service.getTopicWithId("nope")).isEmpty();
    }

    @Test
    void updateTopicOnlyTouchesExistingRows() {
        when(repository.findById("nope")).thenReturn(Optional.empty());
        assertThat(service.updateTopic("nope", new Topic("nope", "x", "y"))).isEmpty();
        verify(repository, never()).save(any());
    }

    @Test
    void filterMinimumLengthForIdUsesCustomPredicate() {
        List<Topic> filtered = service.filterMinimumLengthForId(5);
        assertThat(filtered).extracting(Topic::getId).containsExactly("spring", "javascript");
    }

    @Test
    void sortTopicsWithIdSortsAlphabetically() {
        assertThat(service.sortTopicsWithID()).extracting(Topic::getId)
                .containsExactly("java", "javascript", "spring");
    }

    @Test
    void stringOperationsMatchLegacyBehaviour() {
        String joined = service.returnAllTopicIDWithStringSlicing();
        assertThat(joined).isEqualTo("spring:java:javascript");
        assertThat(service.makeDistinctAndSortCharacters(joined)).isEqualTo(":acgijnprstv");
        assertThat(service.splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoin(joined))
                .isEqualTo("java:javascript");
        assertThat(service.findIdHavingCharacter()).isEqualTo("[spring]");
    }

    @Test
    void topicReportDelegatesToStoredFunction() {
        Map<String, Object> row = Collections.singletonMap("total_topics", 3L);
        when(jdbcTemplate.queryForList("SELECT * FROM topic_report()"))
                .thenReturn(Collections.singletonList(row));
        assertThat(service.topicReport()).containsExactly(row);
    }
}
