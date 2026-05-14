package hello.controller;

import hello.service.TopicService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
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
    public void testDatetime() throws Exception {
        mockMvc.perform(get("/datetime"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Greetings from Spring Boot!")));
    }

    @Test
    public void testDatetimeContainsLeapYear() throws Exception {
        mockMvc.perform(get("/datetime"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Is this a leap year")));
    }

    @Test
    public void testShowStringOperation() throws Exception {
        when(topicService.returnAllTopicIDWithStringSlicing()).thenReturn("spring:java:javascript");
        when(topicService.makeDistinctAndSortCharacters(anyString())).thenReturn("acegijnoprst");
        when(topicService.splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoin(anyString())).thenReturn("java:javascript");
        when(topicService.findIdHavingCharacter()).thenReturn("[spring]");

        mockMvc.perform(get("/topic/string/operation"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("spring:java:javascript")));
    }

    @Test
    public void testShowFileOperation() throws Exception {
        when(topicService.findAllFilesInPathAndSort()).thenReturn("file1; file2");
        when(topicService.findParticularFileInPathAndSort()).thenReturn("gradle_file");
        when(topicService.findParticularFileInPathAndSortWithWalkFunction()).thenReturn("gradle_walk");
        when(topicService.readFileWithStreamFunction()).thenReturn("print_content");

        mockMvc.perform(get("/topic/file/operation"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("file1; file2")));
    }
}
