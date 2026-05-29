package hello.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;

public class GreetingControllerTest {

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = standaloneSetup(new GreetingController()).build();
    }

    @Test
    public void testGreetingDefault() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello, World!"));
    }

    @Test
    public void testGreetingWithName() throws Exception {
        mockMvc.perform(get("/").param("name", "Spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello, Spring!"));
    }
}
