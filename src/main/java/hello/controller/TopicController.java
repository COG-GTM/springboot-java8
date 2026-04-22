package hello.controller;

import hello.model.Topic;
import hello.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
public class TopicController {

    @Autowired
    private TopicService topicService;


    /**
     * Get all Topic
     * @return
     */
    @GetMapping("/topic")
    public ResponseEntity<List<Topic>> getAllTopics() {
        return ResponseEntity.ok(topicService.getAllTopics());
    }

    /**
     * get Topic with ID
     * @param id
     * @return
     */
    @GetMapping("/topic/{id}")
    public ResponseEntity<Topic> getTopicWithID(@PathVariable String id) {
        return ResponseEntity.ok(topicService.getTopicWithId(id));
    }

    /**
     * Add a new topic in list
     * @param topic
     */
    @PostMapping("/topic")
    public ResponseEntity<Void> addTopic(@Valid @RequestBody Topic topic) {
        topicService.addTopic(topic);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Update Topic in List with id
     * @param id
     * @param topic
     */
    @PutMapping("/topic/{id}")
    public ResponseEntity<Void> updateTopic(@PathVariable String id, @Valid @RequestBody Topic topic) {
        topicService.updateTopic(id, topic);
        return ResponseEntity.ok().build();
    }


    /**
     * Delete a topic with ID
     * @param id
     */
    @DeleteMapping("/topic/{id}")
    public ResponseEntity<Void> deleteTopic(@PathVariable String id) {
        topicService.deleteTopic(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all topics with Id length greater then minimum length
     * @param minLength
     * @return
     */
    @GetMapping("/topic/minimum/length/{minLength}")
    public ResponseEntity<List<Topic>> filterMinimumLengthForId(@PathVariable Integer minLength) {
        return ResponseEntity.ok(topicService.filterMinimumLengthForId(minLength));
    }


    /**
     * Sort with Id
     * @return
     */
    @GetMapping("/topic/sort")
    public ResponseEntity<List<Topic>> sortTopicsWithID() {
        return ResponseEntity.ok(topicService.sortTopicsWithID());
    }






}
