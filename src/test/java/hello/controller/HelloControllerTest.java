package hello.controller;

import hello.service.TopicService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(HelloController.class)
@ContextConfiguration(classes = HelloController.class)
public class HelloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TopicService topicService;

    @Test
    public void getsDateTimeGreeting() throws Exception {
        mockMvc.perform(get("/datetime"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Greetings from Spring Boot!")))
                .andExpect(content().string(containsString("Is this a leap year ?")));
    }

    @Test
    public void getsStringOperationResultsWithTemplates() throws Exception {
        when(topicService.returnAllTopicIDWithStringSlicing()).thenReturn("joined-value");
        when(topicService.makeDistinctAndSortCharacters("joined-value")).thenReturn("distinct-value");
        when(topicService.splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoin("joined-value"))
                .thenReturn("java-value");
        when(topicService.findIdHavingCharacter()).thenReturn("character-value");

        mockMvc.perform(get("/topic/string/operation"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("joined-value")))
                .andExpect(content().string(containsString("distinct-value")))
                .andExpect(content().string(containsString("java-value")))
                .andExpect(content().string(containsString("character-value")))
                .andExpect(content().string(containsString("Joining All String ID's with JOIN method: ")))
                .andExpect(content().string(containsString("Get all ID characters")))
                .andExpect(content().string(containsString("Split All Id With Colon")))
                .andExpect(content().string(containsString("Return All ID having character")));
    }

    @Test
    public void getsFileOperationResultsWithTemplates() throws Exception {
        when(topicService.findAllFilesInPathAndSort()).thenReturn("all-files");
        when(topicService.findParticularFileInPathAndSort()).thenReturn("particular-file");
        when(topicService.findParticularFileInPathAndSortWithWalkFunction()).thenReturn("walk-file");
        when(topicService.readFileWithStreamFunction()).thenReturn("read-file");

        mockMvc.perform(get("/topic/file/operation"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("all-files")))
                .andExpect(content().string(containsString("particular-file")))
                .andExpect(content().string(containsString("walk-file")))
                .andExpect(content().string(containsString("read-file")))
                .andExpect(content().string(containsString("Find all files in path and sort")))
                .andExpect(content().string(containsString("Find File in present directory")))
                .andExpect(content().string(containsString("Read \"temp.txt\" file")));
    }
}
