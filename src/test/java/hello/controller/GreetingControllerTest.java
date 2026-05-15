package hello.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class GreetingControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new GreetingController()).build();
    }

    @Test
    public void greeting_defaultName_returnsHelloWorld() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is("Hello, World!")))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    public void greeting_withName_returnsHelloName() throws Exception {
        mockMvc.perform(get("/?name=Devin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is("Hello, Devin!")))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    public void greeting_calledTwice_idIncrements() throws Exception {
        MvcResult first = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult second = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andReturn();

        long firstId = objectMapper.readTree(first.getResponse().getContentAsString()).get("id").asLong();
        long secondId = objectMapper.readTree(second.getResponse().getContentAsString()).get("id").asLong();

        assertTrue("ID should increment", secondId > firstId);
    }
}
