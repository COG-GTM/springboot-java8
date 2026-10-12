package hello.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import hello.service.TopicService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HelloController.class)
class HelloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TopicService topicService;

    // Application (a CommandLineRunner) autowires JdbcTemplate, which the MVC slice doesn't auto-configure.
    @MockBean
    private JdbcTemplate jdbcTemplate;

    @Test
    void datetimeIncludesCaliforniaTime() throws Exception {
        mockMvc.perform(get("/datetime"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Time in California: ")))
                .andExpect(content().string(containsString("[America/Los_Angeles]")));
    }
}
