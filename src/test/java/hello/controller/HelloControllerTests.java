package hello.controller;

import hello.service.TopicService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HelloController.class)
@Import(TopicService.class)
class HelloControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void datetimeEndpointRendersTheGreetingTemplate() throws Exception {
        mockMvc.perform(get("/datetime"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Greetings from Spring Boot!")))
                .andExpect(content().string(containsString("Default system zone id")))
                .andExpect(content().string(containsString("Time in California:")));
    }

    @Test
    void stringOperationEndpointReturnsStreamResults() throws Exception {
        mockMvc.perform(get("/topic/string/operation"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("spring:java:javascript")))
                .andExpect(content().string(containsString("java:javascript")))
                .andExpect(content().string(containsString("[spring]")));
    }
}
