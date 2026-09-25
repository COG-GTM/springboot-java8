package hello.controller;

import hello.model.Topic;
import hello.service.TopicService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class TopicController {

    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @GetMapping("/topic")
    public List<Topic> getAllTopics() {
        return topicService.getAllTopics();
    }

    @GetMapping("/topic/{id}")
    public ResponseEntity<Topic> getTopicWithID(@PathVariable String id) {
        return topicService.getTopicWithId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/topic")
    @ResponseStatus(HttpStatus.CREATED)
    public Topic addTopic(@RequestBody Topic topic) {
        return topicService.addTopic(topic);
    }

    @PutMapping("/topic/{id}")
    public ResponseEntity<Topic> updateTopic(@PathVariable String id, @RequestBody Topic topic) {
        return topicService.updateTopic(id, topic)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/topic/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTopic(@PathVariable String id) {
        topicService.deleteTopic(id);
    }

    @GetMapping("/topic/minimum/length/{minLength}")
    public List<Topic> filterMinimumLengthForId(@PathVariable Integer minLength) {
        return topicService.filterMinimumLengthForId(minLength);
    }

    @GetMapping("/topic/sort")
    public List<Topic> sortTopicsWithID() {
        return topicService.sortTopicsWithID();
    }

    @GetMapping("/topic/report")
    public List<Map<String, Object>> topicReport() {
        return topicService.topicReport();
    }
}
