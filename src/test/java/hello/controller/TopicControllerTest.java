package hello.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import hello.model.Topic;
import hello.service.TopicService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(TopicController.class)
class TopicControllerTest {

    private static final String TOPIC_JSON =
            "{\"id\":\"kotlin\",\"subjectName\":\"Kotlin\",\"subjectDescription\":\"Kotlin Description\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TopicService topicService;

    // Application (a CommandLineRunner) autowires JdbcTemplate, which the MVC slice doesn't auto-configure.
    @MockBean
    private JdbcTemplate jdbcTemplate;

    @Test
    void getAllTopics() throws Exception {
        given(topicService.getAllTopics()).willReturn(List.of(
                new Topic("spring", "Spring Framework", "Spring Framework Description"),
                new Topic("java", "Core Java", "Java Description")));

        mockMvc.perform(get("/topic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value("spring"))
                .andExpect(jsonPath("$[1].subjectName").value("Core Java"));
    }

    @Test
    void getTopicWithIdReturns200() throws Exception {
        given(topicService.getTopicWithId("java"))
                .willReturn(new Topic("java", "Core Java", "Java Description"));

        mockMvc.perform(get("/topic/java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("java"))
                .andExpect(jsonPath("$.subjectName").value("Core Java"))
                .andExpect(jsonPath("$.subjectDescription").value("Java Description"));
    }

    @Test
    void getTopicWithUnknownIdReturns404() throws Exception {
        given(topicService.getTopicWithId("missing"))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found: missing"));

        mockMvc.perform(get("/topic/missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addTopic() throws Exception {
        mockMvc.perform(post("/topic").contentType(MediaType.APPLICATION_JSON).content(TOPIC_JSON))
                .andExpect(status().isOk());

        verify(topicService).addTopic(argThat(t -> "kotlin".equals(t.getId())
                && "Kotlin".equals(t.getSubjectName())
                && "Kotlin Description".equals(t.getSubjectDescription())));
    }

    @Test
    void updateTopic() throws Exception {
        mockMvc.perform(put("/topic/java").contentType(MediaType.APPLICATION_JSON).content(TOPIC_JSON))
                .andExpect(status().isOk());

        verify(topicService).updateTopic(eq("java"), argThat(t -> "kotlin".equals(t.getId())));
    }

    @Test
    void deleteTopic() throws Exception {
        mockMvc.perform(delete("/topic/java"))
                .andExpect(status().isOk());

        verify(topicService).deleteTopic("java");
    }
}
