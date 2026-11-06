package hello.controller;

import hello.model.Topic;
import hello.service.TopicService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TopicControllerValidationTest {

    private TopicService topicService;
    private MockMvc mockMvc;

    @Before
    public void setUp() {
        topicService = new TopicService();
        TopicController topicController = new TopicController();
        HelloController helloController = new HelloController();
        ReflectionTestUtils.setField(topicController, "topicService", topicService);
        ReflectionTestUtils.setField(helloController, "topicService", topicService);
        mockMvc = MockMvcBuilders.standaloneSetup(topicController, helloController).build();
    }

    private static String repeat(char c, int count) {
        return String.join("", Collections.nCopies(count, String.valueOf(c)));
    }

    private static String topicJson(String id) {
        return "{\"id\":\"" + id + "\",\"subjectName\":\"name\",\"subjectDescription\":\"description\"}";
    }

    @Test
    public void postWithMegabyteIdIsRejected() throws Exception {
        int before = topicService.getAllTopics().size();

        mockMvc.perform(post("/topic").contentType(MediaType.APPLICATION_JSON)
                .content(topicJson(repeat('a', 1024 * 1024))))
                .andExpect(status().isBadRequest());

        assertEquals(before, topicService.getAllTopics().size());
    }

    @Test
    public void postWithMissingIdIsRejected() throws Exception {
        mockMvc.perform(post("/topic").contentType(MediaType.APPLICATION_JSON)
                .content("{\"subjectName\":\"name\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void putWithOversizedIdIsRejected() throws Exception {
        mockMvc.perform(put("/topic/java").contentType(MediaType.APPLICATION_JSON)
                .content(topicJson(repeat('a', Topic.MAX_ID_LENGTH + 1))))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void postWithMaximumLengthIdIsAccepted() throws Exception {
        mockMvc.perform(post("/topic").contentType(MediaType.APPLICATION_JSON)
                .content(topicJson(repeat('a', Topic.MAX_ID_LENGTH))))
                .andExpect(status().isOk());
    }

    @Test
    public void postBeyondCapacityReturnsConflict() throws Exception {
        for (int i = topicService.getAllTopics().size(); i < TopicService.MAX_TOPICS; i++) {
            topicService.addTopic(new Topic("id" + i, "name", "description"));
        }

        mockMvc.perform(post("/topic").contentType(MediaType.APPLICATION_JSON)
                .content(topicJson("overflow")))
                .andExpect(status().isConflict());
    }

    @Test(timeout = 2000)
    public void stringOperationStaysFastWithMaximumLengthIds() throws Exception {
        for (int i = 0; i < 100; i++) {
            mockMvc.perform(post("/topic").contentType(MediaType.APPLICATION_JSON)
                    .content(topicJson(repeat('a', Topic.MAX_ID_LENGTH - 3) + String.format("%03d", i))))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(get("/topic/string/operation")).andExpect(status().isOk());
    }
}
