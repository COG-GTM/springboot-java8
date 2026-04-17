package hello.controller;

import hello.model.Topic;
import hello.service.TopicService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TopicController.class)
class TopicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TopicService topicService;

    @MockitoBean
    private JdbcTemplate jdbcTemplate;

    @Test
    void getAllTopics_returnsOk() throws Exception {
        List<Topic> topics = Arrays.asList(
                new Topic("java", "Core Java", "Java Description"),
                new Topic("spring", "Spring", "Spring Description")
        );
        when(topicService.getAllTopics()).thenReturn(topics);

        mockMvc.perform(get("/topic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("java"))
                .andExpect(jsonPath("$[1].id").value("spring"));
    }

    @Test
    void getTopicWithID_existingId_returnsOk() throws Exception {
        Topic topic = new Topic("java", "Core Java", "Java Description");
        when(topicService.getTopicWithId("java")).thenReturn(topic);

        mockMvc.perform(get("/topic/java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("java"))
                .andExpect(jsonPath("$.subjectName").value("Core Java"));
    }

    @Test
    void getTopicWithID_nonExistingId_returns404() throws Exception {
        when(topicService.getTopicWithId("nonexistent"))
                .thenThrow(new NoSuchElementException("Topic not found with id: nonexistent"));

        mockMvc.perform(get("/topic/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addTopic_returnsOk() throws Exception {
        mockMvc.perform(post("/topic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"python\",\"subjectName\":\"Python\",\"subjectDescription\":\"Python Desc\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void updateTopic_returnsOk() throws Exception {
        mockMvc.perform(put("/topic/java")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"java\",\"subjectName\":\"Advanced Java\",\"subjectDescription\":\"Adv Java Desc\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteTopic_returnsOk() throws Exception {
        mockMvc.perform(delete("/topic/java"))
                .andExpect(status().isOk());
    }

    @Test
    void sortTopicsWithID_returnsOk() throws Exception {
        List<Topic> sorted = Arrays.asList(
                new Topic("java", "Core Java", "Java Description"),
                new Topic("spring", "Spring", "Spring Description")
        );
        when(topicService.sortTopicsWithID()).thenReturn(sorted);

        mockMvc.perform(get("/topic/sort"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("java"));
    }

    @Test
    void filterMinimumLengthForId_returnsOk() throws Exception {
        List<Topic> filtered = Arrays.asList(
                new Topic("spring", "Spring", "Spring Description")
        );
        when(topicService.filterMinimumLengthForId(4)).thenReturn(filtered);

        mockMvc.perform(get("/topic/minimum/length/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("spring"));
    }
}
