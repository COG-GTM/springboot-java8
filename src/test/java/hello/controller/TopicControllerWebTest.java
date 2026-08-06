package hello.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end coverage of the JSON contract of the topic endpoints.
 *
 * <p>{@code TopicService} is a singleton holding a mutable list, so anything that writes to it leaks
 * into the next test. Rather than depending on test order, the two tests that mutate the list
 * ({@code /topic} writes and the in-place sort performed by {@code /topic/sort}) drop the context
 * afterwards with {@link DirtiesContext}; every other test can then assume the seeded state.
 *
 * <p>Assertions go through {@code jsonPath} rather than through model getters on purpose: the JSON
 * field names are the actual contract and they survive the ongoing conversion of the models to records.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TopicControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAllTopicsReturnsTheSeededTopics() throws Exception {
        mockMvc.perform(get("/topic"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].id", containsInAnyOrder("spring", "java", "javascript")))
                .andExpect(jsonPath("$[?(@.id=='spring')].subjectName", contains("Spring Framework")))
                .andExpect(jsonPath("$[?(@.id=='spring')].subjectDescription", contains("Spring Framework Description")));
    }

    @Test
    void aTopicSerialisesWithExactlyIdSubjectNameAndSubjectDescription() throws Exception {
        mockMvc.perform(get("/topic/java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(3)))
                .andExpect(jsonPath("$.id").value("java"))
                .andExpect(jsonPath("$.subjectName").value("Core Java"))
                .andExpect(jsonPath("$.subjectDescription").value("Java Description"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void supportsTheFullCreateReadUpdateDeleteLifecycle() throws Exception {
        mockMvc.perform(post("/topic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":"kotlin","subjectName":"Kotlin","subjectDescription":"Kotlin Description"}"""))
                .andExpect(status().isOk());

        mockMvc.perform(get("/topic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[*].id", containsInAnyOrder("spring", "java", "javascript", "kotlin")));

        mockMvc.perform(get("/topic/kotlin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subjectName").value("Kotlin"))
                .andExpect(jsonPath("$.subjectDescription").value("Kotlin Description"));

        mockMvc.perform(put("/topic/kotlin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":"kotlin","subjectName":"Kotlin Updated","subjectDescription":"Updated Description"}"""))
                .andExpect(status().isOk());

        mockMvc.perform(get("/topic/kotlin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subjectName").value("Kotlin Updated"))
                .andExpect(jsonPath("$.subjectDescription").value("Updated Description"));

        mockMvc.perform(delete("/topic/kotlin"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/topic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].id", not(hasItem("kotlin"))));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void sortEndpointReturnsTopicsOrderedById() throws Exception {
        mockMvc.perform(get("/topic/sort"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].id", contains("java", "javascript", "spring")));
    }

    @Test
    void minimumLengthEndpointFiltersOnIdLength() throws Exception {
        mockMvc.perform(get("/topic/minimum/length/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", containsInAnyOrder("spring", "javascript")));

        mockMvc.perform(get("/topic/minimum/length/6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", contains("javascript")));

        mockMvc.perform(get("/topic/minimum/length/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
