package hello.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TopicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAllTopicsReturnsSeededTopics() throws Exception {
        mockMvc.perform(get("/topic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id").value("spring"));
    }

    @Test
    void getTopicById() throws Exception {
        mockMvc.perform(get("/topic/java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subjectName").value("Core Java"));
    }

    @Test
    void addUpdateAndDeleteTopic() throws Exception {
        mockMvc.perform(post("/topic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"kotlin\",\"subjectName\":\"Kotlin\",\"subjectDescription\":\"Kotlin Desc\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/topic/kotlin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subjectName").value("Kotlin"));

        mockMvc.perform(put("/topic/kotlin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"kotlin\",\"subjectName\":\"Kotlin 2\",\"subjectDescription\":\"Updated\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/topic/kotlin"))
                .andExpect(jsonPath("$.subjectName").value("Kotlin 2"));

        mockMvc.perform(delete("/topic/kotlin"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/topic"))
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void filterByMinimumIdLength() throws Exception {
        mockMvc.perform(get("/topic/minimum/length/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void sortTopicsById() throws Exception {
        mockMvc.perform(get("/topic/sort"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("java"))
                .andExpect(jsonPath("$[1].id").value("javascript"))
                .andExpect(jsonPath("$[2].id").value("spring"));
    }
}
