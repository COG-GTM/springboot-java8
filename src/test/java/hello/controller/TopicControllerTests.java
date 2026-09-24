package hello.controller;

import hello.service.TopicService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TopicController.class)
@Import(TopicService.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TopicControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listsAllTopics() throws Exception {
        mockMvc.perform(get("/topic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[?(@.id == 'spring')].subjectName").value("Spring Framework"));
    }

    @Test
    void getsTopicById() throws Exception {
        mockMvc.perform(get("/topic/java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("java"))
                .andExpect(jsonPath("$.subjectName").value("Core Java"))
                .andExpect(jsonPath("$.subjectDescription").value("Java Description"));
    }

    /**
     * Documents current behaviour: {@code TopicService#getTopicWithId} calls
     * {@code Optional#get()}, so an unknown id blows up with an unhandled
     * {@link NoSuchElementException} (a 500 at runtime) rather than a 404.
     */
    @Test
    void getByUnknownIdCurrentlyFailsWithNoSuchElement() {
        assertThatThrownBy(() -> mockMvc.perform(get("/topic/does-not-exist")))
                .hasRootCauseInstanceOf(NoSuchElementException.class);
    }

    @Test
    void createsTopic() throws Exception {
        mockMvc.perform(post("/topic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":"kotlin","subjectName":"Kotlin","subjectDescription":"Kotlin Description"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/topic/kotlin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subjectName").value("Kotlin"));
    }

    @Test
    void updatesTopic() throws Exception {
        mockMvc.perform(put("/topic/spring")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":"spring","subjectName":"Spring Boot","subjectDescription":"Updated Description"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/topic/spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subjectName").value("Spring Boot"));
    }

    /**
     * Documents current behaviour: an update for an unknown id is silently ignored.
     */
    @Test
    void updateOfUnknownIdCurrentlyNoOps() throws Exception {
        mockMvc.perform(put("/topic/does-not-exist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":"does-not-exist","subjectName":"Ghost","subjectDescription":"Ghost"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/topic"))
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void deletesTopic() throws Exception {
        mockMvc.perform(delete("/topic/javascript"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/topic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.id == 'javascript')]").isEmpty());
    }

    @Test
    void filtersTopicsByMinimumIdLength() throws Exception {
        mockMvc.perform(get("/topic/minimum/length/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void sortsTopicsById() throws Exception {
        mockMvc.perform(get("/topic/sort"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("java"))
                .andExpect(jsonPath("$[1].id").value("javascript"))
                .andExpect(jsonPath("$[2].id").value("spring"));
    }
}
