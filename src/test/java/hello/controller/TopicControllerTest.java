package hello.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TopicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void existingTopicReturnsOk() throws Exception {
        mockMvc.perform(get("/topic/java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subjectName").value("Core Java"));
    }

    @Test
    void missingTopicReturnsNotFound() throws Exception {
        mockMvc.perform(get("/topic/does-not-exist"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatingMissingTopicReturnsNotFound() throws Exception {
        mockMvc.perform(put("/topic/does-not-exist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"does-not-exist\",\"subjectName\":\"x\",\"subjectDescription\":\"y\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void sortDoesNotMutateTopicOrder() throws Exception {
        mockMvc.perform(get("/topic/sort"))
                .andExpect(jsonPath("$[0].id").value("java"));
        mockMvc.perform(get("/topic"))
                .andExpect(jsonPath("$[0].id").value("spring"));
    }

    @Test
    void fileOperationReadsTempFileFromClasspath() throws Exception {
        mockMvc.perform(get("/topic/file/operation"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(" Hello, this, is, Rehman")));
    }
}
