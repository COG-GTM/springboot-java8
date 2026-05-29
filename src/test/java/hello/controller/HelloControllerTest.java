package hello.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import hello.service.TopicService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;

public class HelloControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TopicService topicService;

    @InjectMocks
    private HelloController helloController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = standaloneSetup(helloController).build();
    }

    @Test
    public void testDatetime() throws Exception {
        mockMvc.perform(get("/datetime"))
                .andExpect(status().isOk());
    }

    @Test
    public void testStringOperation() throws Exception {
        when(topicService.returnAllTopicIDWithStringSlicing()).thenReturn("java:spring");
        when(topicService.makeDistinctAndSortCharacters("java:spring")).thenReturn(":agjnprv");
        when(topicService.splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoin("java:spring"))
                .thenReturn("java");
        when(topicService.findIdHavingCharacter()).thenReturn("[spring]");

        mockMvc.perform(get("/topic/string/operation"))
                .andExpect(status().isOk());
    }

    @Test
    public void testFileOperation() throws Exception {
        when(topicService.findAllFilesInPathAndSort()).thenReturn("file1; file2");
        when(topicService.findParticularFileInPathAndSort()).thenReturn("gradle");
        when(topicService.findParticularFileInPathAndSortWithWalkFunction()).thenReturn("gradle");
        when(topicService.readFileWithStreamFunction()).thenReturn("line1");

        mockMvc.perform(get("/topic/file/operation"))
                .andExpect(status().isOk());
    }
}
