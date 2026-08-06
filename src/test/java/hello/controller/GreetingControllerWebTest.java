package hello.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pins the JSON contract of {@code /}: {@code {"id": <number>, "content": <string>}}.
 */
@SpringBootTest
@AutoConfigureMockMvc
class GreetingControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void greetsTheWorldByDefault() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.*", hasSize(2)))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.content").value("Hello, World!"));
    }

    @Test
    void greetsTheRequestedName() throws Exception {
        mockMvc.perform(get("/").param("name", "Devin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello, Devin!"));
    }

    @Test
    void everyGreetingGetsItsOwnId() throws Exception {
        long first = greetingId();
        long second = greetingId();

        assertThat(second).isEqualTo(first + 1);
    }

    private long greetingId() throws Exception {
        String json = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.parse(json).read("$.id", Long.class);
    }
}
