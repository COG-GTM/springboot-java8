package hello.controller;

import hello.service.TopicService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class HelloControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TopicService topicService;

    @InjectMocks
    private HelloController helloController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(helloController).build();
    }

    @Test
    public void datetime_returns200WithExpectedSubstrings() throws Exception {
        mockMvc.perform(get("/datetime"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Datetime now is")))
                .andExpect(content().string(containsString("leap year")));
    }

    @Test
    public void showStringOperation_returns200WithAssembledResponse() throws Exception {
        when(topicService.returnAllTopicIDWithStringSlicing()).thenReturn("spring:java:javascript");
        when(topicService.makeDistinctAndSortCharacters("spring:java:javascript")).thenReturn(":acgijnoprstvw");
        when(topicService.splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoin("spring:java:javascript"))
                .thenReturn("java:javascript");
        when(topicService.findIdHavingCharacter()).thenReturn("[spring, javascript]");

        mockMvc.perform(get("/topic/string/operation"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("spring:java:javascript")))
                .andExpect(content().string(containsString(":acgijnoprstvw")))
                .andExpect(content().string(containsString("java:javascript")))
                .andExpect(content().string(containsString("[spring, javascript]")));
    }

    @Test
    public void showFileOperation_returns200WithAssembledResponse() throws Exception {
        when(topicService.findAllFilesInPathAndSort()).thenReturn("build.gradle; pom.xml");
        when(topicService.findParticularFileInPathAndSort()).thenReturn("gradle; gradlew");
        when(topicService.findParticularFileInPathAndSortWithWalkFunction()).thenReturn("gradle; gradlew");
        when(topicService.readFileWithStreamFunction()).thenReturn(" Hello, World");

        mockMvc.perform(get("/topic/file/operation"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("build.gradle; pom.xml")))
                .andExpect(content().string(containsString("gradle; gradlew")))
                .andExpect(content().string(containsString(" Hello, World")));
    }
}
