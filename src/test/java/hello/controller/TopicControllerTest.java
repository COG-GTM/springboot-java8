package hello.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.fasterxml.jackson.databind.ObjectMapper;
import hello.model.Topic;
import hello.service.TopicService;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

public class TopicControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TopicService topicService;

    @InjectMocks
    private TopicController topicController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = standaloneSetup(topicController).build();
    }

    @Test
    public void testGetAllTopics() throws Exception {
        List<Topic> topics = Arrays.asList(
                new Topic("java", "Core Java", "Java Description"),
                new Topic("spring", "Spring", "Spring Description"));
        when(topicService.getAllTopics()).thenReturn(topics);

        mockMvc.perform(get("/topic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("java"));
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
        Topic topic = new Topic("python", "Python", "Python Desc");

        mockMvc.perform(post("/topic")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(topic)))
                .andExpect(status().isOk());

        verify(topicService).addTopic(any(Topic.class));
    }

    @Test
    public void testUpdateTopic() throws Exception {
        Topic topic = new Topic("java", "Java Updated", "Updated Desc");

        mockMvc.perform(put("/topic/java")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(topic)))
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
                new Topic("javascript", "JS", "JS Desc"));
        when(topicService.filterMinimumLengthForId(5)).thenReturn(filtered);

        mockMvc.perform(get("/topic/minimum/length/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    public void testSortTopicsWithID() throws Exception {
        List<Topic> sorted = Arrays.asList(
                new Topic("java", "Java", "Desc"),
                new Topic("spring", "Spring", "Desc"));
        when(topicService.sortTopicsWithID()).thenReturn(sorted);

        mockMvc.perform(get("/topic/sort"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("java"));
    }
}
