package hello.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class GreetingAndHelloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void greetingDefaultsToWorld() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello, World!"));
    }

    @Test
    public void greetingUsesNameParam() throws Exception {
        mockMvc.perform(get("/").param("name", "Devin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello, Devin!"));
    }

    @Test
    public void datetimeEndpoint() throws Exception {
        mockMvc.perform(get("/datetime"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Greetings from Spring Boot!")))
                .andExpect(content().string(containsString("Time in California:")));
    }

    @Test
    public void stringOperationEndpoint() throws Exception {
        mockMvc.perform(get("/topic/string/operation"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("spring:java:javascript")))
                .andExpect(content().string(containsString("java:javascript")))
                .andExpect(content().string(containsString("[spring]")));
    }

    @Test
    public void fileOperationEndpoint() throws Exception {
        mockMvc.perform(get("/topic/file/operation"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Read \"temp.txt\"")))
                .andExpect(content().string(containsString(" Hello, this, is, Rehman")));
    }
}
