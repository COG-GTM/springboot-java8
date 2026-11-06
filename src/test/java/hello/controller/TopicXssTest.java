package hello.controller;

import hello.model.Topic;
import hello.service.TopicService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TopicXssTest {

    private static final String SCRIPT_ID = "<script>alert(document.domain)</script>";
    private static final String BROWSER_ACCEPT =
            "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";

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

    private static String topicJson(String id) {
        return "{\"id\":\"" + id + "\",\"subjectName\":\"x\",\"subjectDescription\":\"y\"}";
    }

    private boolean hasTopicId(String id) {
        return topicService.getAllTopics().stream().anyMatch(t -> id.equals(t.getId()));
    }

    @Test
    public void postRejectsScriptId() throws Exception {
        mockMvc.perform(post("/topic").contentType(MediaType.APPLICATION_JSON).content(topicJson(SCRIPT_ID)))
                .andExpect(status().isBadRequest());
        assertFalse(hasTopicId(SCRIPT_ID));
    }

    @Test
    public void postRejectsBlankAndOverlongIds() throws Exception {
        mockMvc.perform(post("/topic").contentType(MediaType.APPLICATION_JSON).content(topicJson("")))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/topic").contentType(MediaType.APPLICATION_JSON)
                .content("{\"subjectName\":\"x\",\"subjectDescription\":\"y\"}"))
                .andExpect(status().isBadRequest());
        String overlong = new String(new char[65]).replace('\0', 'a');
        mockMvc.perform(post("/topic").contentType(MediaType.APPLICATION_JSON).content(topicJson(overlong)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void postAcceptsPlainId() throws Exception {
        mockMvc.perform(post("/topic").contentType(MediaType.APPLICATION_JSON).content(topicJson("kotlin_1-x")))
                .andExpect(status().isOk());
        assertTrue(hasTopicId("kotlin_1-x"));
    }

    @Test
    public void putRejectsScriptId() throws Exception {
        mockMvc.perform(put("/topic/java").contentType(MediaType.APPLICATION_JSON).content(topicJson(SCRIPT_ID)))
                .andExpect(status().isBadRequest());
        assertFalse(hasTopicId(SCRIPT_ID));
        assertTrue(hasTopicId("java"));
    }

    @Test
    public void stringOperationIsServedAsPlainTextToBrowsers() throws Exception {
        topicService.addTopic(new Topic(SCRIPT_ID, "x", "y"));

        mockMvc.perform(get("/topic/string/operation").header("Accept", BROWSER_ACCEPT))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string(containsString("spring:java:javascript")));
    }

    @Test
    public void stringOperationIsNeverServedAsHtml() throws Exception {
        mockMvc.perform(get("/topic/string/operation").header("Accept", MediaType.TEXT_HTML_VALUE))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    public void otherStringEndpointsAreServedAsPlainText() throws Exception {
        mockMvc.perform(get("/datetime").header("Accept", BROWSER_ACCEPT))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN));
        mockMvc.perform(get("/topic/file/operation").header("Accept", BROWSER_ACCEPT))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN));
    }
}
