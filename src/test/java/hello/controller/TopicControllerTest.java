package hello.controller;

import hello.model.Topic;
import hello.service.TopicService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private MockMvc mockMvc;

    @MockitoBean
    private TopicService topicService;

    @Test
    void listsTopics() throws Exception {
        given(topicService.getAllTopics()).willReturn(List.of(new Topic("spring", "Spring Framework", "Description")));

        mockMvc.perform(get("/topic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("spring"))
                .andExpect(jsonPath("$[0].subjectName").value("Spring Framework"));
    }

    @Test
    void returnsSingleTopic() throws Exception {
        given(topicService.getTopicWithId("java")).willReturn(new Topic("java", "Core Java", "Description"));

        mockMvc.perform(get("/topic/java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subjectName").value("Core Java"));
    }

    @Test
    void createsTopicFromJsonBody() throws Exception {
        mockMvc.perform(post("/topic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"kotlin\",\"subjectName\":\"Kotlin\",\"subjectDescription\":\"Desc\"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<Topic> captor = ArgumentCaptor.forClass(Topic.class);
        verify(topicService).addTopic(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo("kotlin");
    }

    @Test
    void deletesTopic() throws Exception {
        mockMvc.perform(delete("/topic/java")).andExpect(status().isOk());

        verify(topicService).deleteTopic("java");
    }
}
