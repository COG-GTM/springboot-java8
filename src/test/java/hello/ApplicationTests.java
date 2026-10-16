package hello;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void greetingReturnsDefaultName() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello, World!"));
    }

    @Test
    void greetingRejectsNonGetMethods() throws Exception {
        mockMvc.perform(post("/"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void topicWithExistingIdIsReturned() throws Exception {
        mockMvc.perform(get("/topic/java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subjectName").value("Core Java"));
    }

    @Test
    void topicWithMissingIdReturns404() throws Exception {
        mockMvc.perform(get("/topic/does-not-exist"))
                .andExpect(status().isNotFound());
    }

    @Test
    void datetimeUsesCaliforniaZone() throws Exception {
        mockMvc.perform(get("/datetime"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("[America/Los_Angeles]")));
    }

    @Test
    void fileOperationReadsPrintLines() throws Exception {
        mockMvc.perform(get("/topic/file/operation"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(" Hello, this, is, Rehman")));
    }
}
