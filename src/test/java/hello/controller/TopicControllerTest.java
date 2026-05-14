package hello.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hello.model.Topic;
import hello.service.TopicService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TopicControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TopicService topicService;

    @InjectMocks
    private TopicController topicController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(topicController).build();
    }

    @Test
    public void testGetAllTopics() throws Exception {
        List<Topic> topics = Arrays.asList(
                new Topic("java", "Core Java", "Java Description"),
                new Topic("spring", "Spring Framework", "Spring Description")
        );
        when(topicService.getAllTopics()).thenReturn(topics);

        mockMvc.perform(get("/topic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("java"))
                .andExpect(jsonPath("$[1].id").value("spring"));
    }

    @Test
    public void testGetTopicWithID() throws Exception {
        Topic topic = new Topic("java", "Core Java", "Java Description");
        when(topicService.getTopicWithId("java")).thenReturn(topic);

        mockMvc.perform(get("/topic/java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("java"))
                .andExpect(jsonPath("$.subjectName").value("Core Java"));
    }

    @Test
    public void testAddTopic() throws Exception {
        Topic topic = new Topic("python", "Python", "Python Description");

        mockMvc.perform(post("/topic")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(topic)))
                .andExpect(status().isOk());

        verify(topicService).addTopic(any(Topic.class));
    }

    @Test
    public void testUpdateTopic() throws Exception {
        Topic topic = new Topic("java", "Updated Java", "Updated Description");

        mockMvc.perform(put("/topic/java")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(topic)))
                .andExpect(status().isOk());

        verify(topicService).updateTopic(eq("java"), any(Topic.class));
    }

    @Test
    public void testDeleteTopic() throws Exception {
        mockMvc.perform(delete("/topic/java"))
                .andExpect(status().isOk());

        verify(topicService).deleteTopic("java");
    }

    @Test
    public void testFilterMinimumLengthForId() throws Exception {
        List<Topic> filtered = Arrays.asList(
                new Topic("spring", "Spring Framework", "Spring Description"),
                new Topic("javascript", "JavaScript", "JS Description")
        );
        when(topicService.filterMinimumLengthForId(4)).thenReturn(filtered);

        mockMvc.perform(get("/topic/minimum/length/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("spring"));
    }

    @Test
    public void testSortTopicsWithID() throws Exception {
        List<Topic> sorted = Arrays.asList(
                new Topic("java", "Core Java", "Java Description"),
                new Topic("javascript", "JavaScript", "JS Description"),
                new Topic("spring", "Spring Framework", "Spring Description")
        );
        when(topicService.sortTopicsWithID()).thenReturn(sorted);

        mockMvc.perform(get("/topic/sort"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("java"));
    }
}
