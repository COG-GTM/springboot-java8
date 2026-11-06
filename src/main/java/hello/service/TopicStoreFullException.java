package hello.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Topic store is full")
public class TopicStoreFullException extends RuntimeException {

    public TopicStoreFullException(int maxTopics) {
        super("Topic store is full (max " + maxTopics + " topics)");
    }
}
