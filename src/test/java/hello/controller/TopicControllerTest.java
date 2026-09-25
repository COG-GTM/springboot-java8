package hello.controller;

import hello.model.Topic;
import hello.service.TopicService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TopicController.class)
class TopicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TopicService topicService;

    @Test
    void listsTopics() throws Exception {
        when(topicService.getAllTopics()).thenReturn(Arrays.asList(
                new Topic("spring", "Spring Framework", "desc"),
                new Topic("java", "Core Java", "desc")));

        mockMvc.perform(get("/topic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("spring"))
                .andExpect(jsonPath("$[1].subjectName").value("Core Java"));
    }

    @Test
    void returns404ForUnknownTopic() throws Exception {
        when(topicService.getTopicWithId("nope")).thenReturn(Optional.empty());
        mockMvc.perform(get("/topic/nope")).andExpect(status().isNotFound());
    }

    @Test
    void createsTopic() throws Exception {
        Topic topic = new Topic("k8s", "Kubernetes", "desc");
        when(topicService.addTopic(any(Topic.class))).thenReturn(topic);

        mockMvc.perform(post("/topic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"k8s\",\"subjectName\":\"Kubernetes\",\"subjectDescription\":\"desc\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("k8s"));
    }
}
