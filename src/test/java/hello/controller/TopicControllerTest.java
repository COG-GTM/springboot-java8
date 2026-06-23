package hello.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TopicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getAllTopicsReturnsThreeDefaults() throws Exception {
        mockMvc.perform(get("/topic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    public void getTopicByIdReturnsJavaTopic() throws Exception {
        mockMvc.perform(get("/topic/java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("java")))
                .andExpect(jsonPath("$.subjectName", is("Core Java")));
    }

    @Test
    public void postTopicAddsTopic() throws Exception {
        mockMvc.perform(post("/topic")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"python\",\"subjectName\":\"Python\",\"subjectDescription\":\"Python Description\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/topic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));
        mockMvc.perform(get("/topic/python"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subjectName", is("Python")));
    }

    @Test
    public void putTopicUpdatesJavaTopic() throws Exception {
        mockMvc.perform(put("/topic/java")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"java\",\"subjectName\":\"Updated Java\",\"subjectDescription\":\"Updated\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/topic/java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subjectName", is("Updated Java")));
    }

    @Test
    public void deleteTopicRemovesJavaTopic() throws Exception {
        mockMvc.perform(delete("/topic/java"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/topic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    public void minimumLengthFiltersTopicsByIdLength() throws Exception {
        mockMvc.perform(get("/topic/minimum/length/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("spring")))
                .andExpect(jsonPath("$[1].id", is("javascript")));
    }

    @Test
    public void sortReturnsTopicsSortedById() throws Exception {
        mockMvc.perform(get("/topic/sort"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is("java")))
                .andExpect(jsonPath("$[1].id", is("javascript")))
                .andExpect(jsonPath("$[2].id", is("spring")));
    }
}
