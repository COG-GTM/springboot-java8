package hello.service;

import hello.model.Topic;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TopicServiceTest {

    private final TopicService service = new TopicService();

    @Test
    void missingTopicThrowsNotFound() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.getTopicWithId("missing"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void sortDoesNotReorderSharedList() {
        List<Topic> sorted = service.sortTopicsWithID();
        assertEquals(List.of("java", "javascript", "spring"), sorted.stream().map(Topic::getId).toList());
        assertEquals(List.of("spring", "java", "javascript"), service.getAllTopics().stream().map(Topic::getId).toList());
    }

    @Test
    void getAllTopicsReturnsDefensiveCopy() {
        service.getAllTopics().clear();
        assertEquals(3, service.getAllTopics().size());
    }

    @Test
    void overlappingUpdatesAndDeletesDoNotFail() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < 2000; i++) {
                String id = "t" + (i % 20);
                futures.add(pool.submit(() -> service.addTopic(new Topic(id, "n", "d"))));
                futures.add(pool.submit(() -> service.updateTopic(id, new Topic(id, "updated", "d"))));
                futures.add(pool.submit(() -> service.deleteTopic(id)));
            }
            for (Future<?> future : futures) {
                future.get();
            }
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void readFileStripsPrintPrefix() {
        assertEquals(" Hello, this, is, Rehman", service.readFileWithStreamFunction());
    }
}
