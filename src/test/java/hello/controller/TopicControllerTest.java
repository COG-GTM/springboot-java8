package hello.controller;

import hello.model.Topic;
import hello.service.TopicService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(TopicController.class)
@ContextConfiguration(classes = TopicController.class)
public class TopicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TopicService topicService;

    @Test
    public void getsAllTopics() throws Exception {
        when(topicService.getAllTopics()).thenReturn(Arrays.asList(
                new Topic("spring", "Spring", "Description"),
                new Topic("java", "Java", "Description")));

        mockMvc.perform(get("/topic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("spring")))
                .andExpect(jsonPath("$[1].subjectName", is("Java")));
    }

    @Test
    public void getsTopicById() throws Exception {
        when(topicService.getTopicWithId("java"))
                .thenReturn(new Topic("java", "Java", "Description"));

        mockMvc.perform(get("/topic/java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("java")))
                .andExpect(jsonPath("$.subjectDescription", is("Description")));
    }

    @Test
    public void postsTopic() throws Exception {
        mockMvc.perform(post("/topic")
                        .contentType("application/json")
                        .content("{\"id\":\"kotlin\",\"subjectName\":\"Kotlin\",\"subjectDescription\":\"Description\"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<Topic> captor = ArgumentCaptor.forClass(Topic.class);
        verify(topicService).addTopic(captor.capture());
        assertTopic(captor.getValue(), "kotlin", "Kotlin", "Description");
    }

    @Test
    public void putsTopic() throws Exception {
        mockMvc.perform(put("/topic/java")
                        .contentType("application/json")
                        .content("{\"id\":\"java\",\"subjectName\":\"Updated\",\"subjectDescription\":\"Updated description\"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<Topic> captor = ArgumentCaptor.forClass(Topic.class);
        verify(topicService).updateTopic(eq("java"), captor.capture());
        assertTopic(captor.getValue(), "java", "Updated", "Updated description");
    }

    @Test
    public void deletesTopic() throws Exception {
        mockMvc.perform(delete("/topic/java"))
                .andExpect(status().isOk());

        verify(topicService).deleteTopic("java");
    }

    @Test
    public void filtersTopicsByMinimumLength() throws Exception {
        when(topicService.filterMinimumLengthForId(4))
                .thenReturn(Arrays.asList(new Topic("spring", "Spring", "Description")));

        mockMvc.perform(get("/topic/minimum/length/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is("spring")));
    }

    @Test
    public void sortsTopics() throws Exception {
        when(topicService.sortTopicsWithID()).thenReturn(Arrays.asList(
                new Topic("java", "Java", "Description"),
                new Topic("spring", "Spring", "Description")));

        mockMvc.perform(get("/topic/sort"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is("java")))
                .andExpect(jsonPath("$[1].id", is("spring")));
    }

    private static void assertTopic(Topic topic, String id, String name, String description) {
        org.junit.Assert.assertEquals(id, topic.getId());
        org.junit.Assert.assertEquals(name, topic.getSubjectName());
        org.junit.Assert.assertEquals(description, topic.getSubjectDescription());
    }
}
