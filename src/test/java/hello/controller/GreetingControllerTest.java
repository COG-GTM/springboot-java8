package hello.controller;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class GreetingControllerTest {

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new GreetingController()).build();
    }

    @Test
    public void testGreetingDefault() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello, World!"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    public void testGreetingWithName() throws Exception {
        mockMvc.perform(get("/").param("name", "Bob"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello, Bob!"));
    }

    @Test
    public void testGreetingIncrementsId() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }
}
