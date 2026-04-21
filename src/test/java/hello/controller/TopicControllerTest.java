package hello.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hello.model.Topic;
import hello.service.TopicService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TopicController.class)
class TopicControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    TopicService topicService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void getAllTopics_returnsList() throws Exception {
        given(topicService.getAllTopics()).willReturn(List.of(
                new Topic("spring", "Spring Framework", "desc")));

        mockMvc.perform(get("/topic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("spring"))
                .andExpect(jsonPath("$[0].subjectName").value("Spring Framework"));
    }

    @Test
    void getTopicById_whenMissing_returns404() throws Exception {
        given(topicService.getTopicWithId("missing")).willReturn(Optional.empty());

        mockMvc.perform(get("/topic/missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTopicById_whenPresent_returnsTopic() throws Exception {
        given(topicService.getTopicWithId("java"))
                .willReturn(Optional.of(new Topic("java", "Core Java", "desc")));

        mockMvc.perform(get("/topic/java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("java"));
    }

    @Test
    void addTopic_returns201() throws Exception {
        Topic topic = new Topic("go", "Go", "desc");
        mockMvc.perform(post("/topic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(topic)))
                .andExpect(status().isCreated());

        verify(topicService).addTopic(any(Topic.class));
    }

    @Test
    void deleteTopic_returns204() throws Exception {
        mockMvc.perform(delete("/topic/java"))
                .andExpect(status().isNoContent());

        verify(topicService).deleteTopic("java");
    }
}
