package hello.controller;

import hello.model.Topic;
import hello.service.TopicService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TopicControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TopicService topicService;

    @InjectMocks
    private TopicController topicController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(topicController).build();
    }

    @Test
    public void getAllTopics_returns200WithJsonArray() throws Exception {
        List<Topic> topics = Arrays.asList(
                new Topic("spring", "Spring Framework", "Spring Description"),
                new Topic("java", "Core Java", "Java Description")
        );
        when(topicService.getAllTopics()).thenReturn(topics);

        mockMvc.perform(get("/topic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("spring")))
                .andExpect(jsonPath("$[1].id", is("java")));
    }

    @Test
    public void getTopicWithID_returns200WithJsonObject() throws Exception {
        Topic topic = new Topic("spring", "Spring Framework", "Spring Description");
        when(topicService.getTopicWithId("spring")).thenReturn(topic);

        mockMvc.perform(get("/topic/spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("spring")))
                .andExpect(jsonPath("$.subjectName", is("Spring Framework")))
                .andExpect(jsonPath("$.subjectDescription", is("Spring Description")));
    }

    @Test
    public void addTopic_callsServiceAddTopic() throws Exception {
        Topic topic = new Topic("python", "Python", "Python Desc");

        mockMvc.perform(post("/topic")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(topic)))
                .andExpect(status().isOk());

        verify(topicService, times(1)).addTopic(any(Topic.class));
    }

    @Test
    public void updateTopic_callsServiceUpdateTopic() throws Exception {
        Topic topic = new Topic("spring", "Spring Boot", "Updated Desc");

        mockMvc.perform(put("/topic/spring")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(topic)))
                .andExpect(status().isOk());

        verify(topicService, times(1)).updateTopic(eq("spring"), any(Topic.class));
    }

    @Test
    public void deleteTopic_callsServiceDeleteTopic() throws Exception {
        mockMvc.perform(delete("/topic/spring"))
                .andExpect(status().isOk());

        verify(topicService, times(1)).deleteTopic("spring");
    }

    @Test
    public void filterMinimumLengthForId_returns200WithFilteredTopics() throws Exception {
        List<Topic> filtered = Arrays.asList(
                new Topic("spring", "Spring Framework", "Spring Description"),
                new Topic("javascript", "JavaScript", "JS Description")
        );
        when(topicService.filterMinimumLengthForId(4)).thenReturn(filtered);

        mockMvc.perform(get("/topic/minimum/length/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("spring")))
                .andExpect(jsonPath("$[1].id", is("javascript")));
    }

    @Test
    public void sortTopicsWithID_returns200WithSortedTopics() throws Exception {
        List<Topic> sorted = Arrays.asList(
                new Topic("java", "Core Java", "Java Description"),
                new Topic("javascript", "JavaScript", "JS Description"),
                new Topic("spring", "Spring Framework", "Spring Description")
        );
        when(topicService.sortTopicsWithID()).thenReturn(sorted);

        mockMvc.perform(get("/topic/sort"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is("java")))
                .andExpect(jsonPath("$[1].id", is("javascript")))
                .andExpect(jsonPath("$[2].id", is("spring")));
    }
}
