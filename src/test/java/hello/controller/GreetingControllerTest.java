package hello.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class GreetingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void greetingDefaultsToWorld() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is("Hello, World!")));
    }

    @Test
    public void greetingUsesProvidedName() throws Exception {
        mockMvc.perform(get("/").param("name", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is("Hello, Test!")));
    }
}
