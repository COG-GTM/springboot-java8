package hello.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hello.exception.GlobalExceptionHandler;
import hello.exception.TopicNotFoundException;
import hello.model.Topic;
import hello.service.TopicService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TopicController.class)
@Import(GlobalExceptionHandler.class)
class TopicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TopicService topicService;

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllTopicsReturns200WithJsonArray() throws Exception {
        given(topicService.getAllTopics()).willReturn(Arrays.asList(
                new Topic("java", "Core Java", "Java desc"),
                new Topic("spring", "Spring Framework", "Spring desc")
        ));

        mockMvc.perform(get("/topic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$[0].id").value("java"));
    }

    @Test
    void getTopicByIdReturns200WhenFound() throws Exception {
        given(topicService.getTopicWithId("java"))
                .willReturn(new Topic("java", "Core Java", "Java desc"));

        mockMvc.perform(get("/topic/java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("java"))
                .andExpect(jsonPath("$.subjectName").value("Core Java"));
    }

    @Test
    void getTopicByIdReturns404WhenMissing() throws Exception {
        given(topicService.getTopicWithId("missing"))
                .willThrow(new TopicNotFoundException("Topic not found with id: missing"));

        mockMvc.perform(get("/topic/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Topic not found with id: missing"));
    }

    @Test
    void createTopicReturns201() throws Exception {
        Topic topic = new Topic("python", "Python", "Python desc");

        mockMvc.perform(post("/topic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(topic)))
                .andExpect(status().isCreated());

        verify(topicService).addTopic(any(Topic.class));
    }

    @Test
    void updateTopicReturns200() throws Exception {
        Topic topic = new Topic("java", "Java 17", "Updated desc");

        mockMvc.perform(put("/topic/java")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(topic)))
                .andExpect(status().isOk());

        verify(topicService).updateTopic(eq("java"), any(Topic.class));
    }

    @Test
    void deleteTopicReturns204() throws Exception {
        mockMvc.perform(delete("/topic/java"))
                .andExpect(status().isNoContent());

        verify(topicService).deleteTopic("java");
    }

    @Test
    void genericExceptionReturns500() throws Exception {
        willThrow(new RuntimeException("boom")).given(topicService).deleteTopic("boom");

        mockMvc.perform(delete("/topic/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("boom"));
    }
}
