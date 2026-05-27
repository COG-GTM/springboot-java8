package hello;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void contextLoads() {
    }

    @Test
    public void greetingEndpointReturnsDefaultGreeting() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello, World!"));
    }

    @Test
    public void greetingEndpointReturnsCustomName() throws Exception {
        mockMvc.perform(get("/").param("name", "Spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello, Spring!"));
    }

    @Test
    public void getAllTopicsReturnsThreeTopics() throws Exception {
        mockMvc.perform(get("/topic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value("spring"))
                .andExpect(jsonPath("$[1].id").value("java"))
                .andExpect(jsonPath("$[2].id").value("javascript"));
    }

    @Test
    public void getTopicByIdReturnsCorrectTopic() throws Exception {
        mockMvc.perform(get("/topic/java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("java"))
                .andExpect(jsonPath("$.subjectName").value("Core Java"));
    }

    @Test
    public void datetimeEndpointReturnsContent() throws Exception {
        mockMvc.perform(get("/datetime"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Greetings from Spring Boot!")));
    }

    @Test
    public void stringOperationEndpointReturnsContent() throws Exception {
        mockMvc.perform(get("/topic/string/operation"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Joining All String ID's with JOIN method")));
    }

    @Test
    public void sortTopicsEndpointReturnsSortedTopics() throws Exception {
        mockMvc.perform(get("/topic/sort"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("java"))
                .andExpect(jsonPath("$[1].id").value("javascript"))
                .andExpect(jsonPath("$[2].id").value("spring"));
    }

    @Test
    public void filterMinimumLengthReturnsFilteredTopics() throws Exception {
        mockMvc.perform(get("/topic/minimum/length/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
