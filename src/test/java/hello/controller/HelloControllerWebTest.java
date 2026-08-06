package hello.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesRegex;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Covers the plain-text endpoints. {@code /topic/file/operation} walks the process working directory
 * and reads {@code temp.txt} from it, so its exact output changes whenever a file is added to or
 * removed from the repository; only the stable parts of the response are asserted.
 */
@SpringBootTest
@AutoConfigureMockMvc
class HelloControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void datetimeEndpointRendersTheJavaTimeShowcase() throws Exception {
        mockMvc.perform(get("/datetime"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Greetings from Spring Boot!")))
                .andExpect(content().string(containsString("Datetime now is ")))
                .andExpect(content().string(containsString("Is this a leap year ?")))
                .andExpect(content().string(containsString("Default system zone id")))
                .andExpect(content().string(containsString("Time in California: ")))
                // "Datetime now is <ISO local date time>"
                .andExpect(content().string(matchesRegex("(?s).*Datetime now is \\d{4}-\\d{2}-\\d{2}T[\\d:.]+.*")));
    }

    @Test
    void stringOperationEndpointRendersTheStreamShowcase() throws Exception {
        mockMvc.perform(get("/topic/string/operation"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Joining All String ID's with JOIN method: ")))
                // The distinct-and-sorted characters and the ":"-split are both sorted, so they are
                // stable even if another test has re-ordered the singleton's topic list.
                .andExpect(content().string(containsString(":acgijnprstv")))
                .andExpect(content().string(containsString("java:javascript")))
                .andExpect(content().string(containsString("[spring]")));
    }

    @Test
    void fileOperationEndpointRendersTheNioShowcase() throws Exception {
        mockMvc.perform(get("/topic/file/operation"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Find all files in path and sort:")))
                .andExpect(content().string(containsString("pom.xml")))
                .andExpect(content().string(containsString("Read \"temp.txt\" file with stream functions")))
                .andExpect(content().string(not(containsString("Error in IO"))))
                .andExpect(content().string(not(containsString("IO exception"))));
    }
}
