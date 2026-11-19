package hello;

import hello.model.Quote;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApplicationTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Application application;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    void contextLoadsAndInitializesDatabase() {
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM customers", Integer.class)).isEqualTo(4);
    }

    @Test
    void greetingUsesDefaultAndProvidedName() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.content").value("Hello, World!"));
        mvc.perform(get("/").param("name", "Java 8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello, Java 8!"));
    }

    @Test
    void topicsReturnExistingData() throws Exception {
        mvc.perform(get("/topic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
        mvc.perform(get("/topic/java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("java"))
                .andExpect(jsonPath("$.subjectName").value("Core Java"))
                .andExpect(jsonPath("$.subjectDescription").value("Java Description"));
    }

    @Test
    void topicFilterCanReturnEmptyList() throws Exception {
        mvc.perform(get("/topic/minimum/length/100"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void invalidRequestsAreRejected() throws Exception {
        mvc.perform(get("/topic/minimum/length/not-a-number"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/topic").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void quoteDemoHandlesResponseAndUnavailableService() throws Exception {
        when(restTemplate.getForObject("http://gturnquist-quoters.cfapps.io/api/random", Quote.class))
                .thenReturn(new Quote())
                .thenThrow(new ResourceAccessException("Unavailable"));
        application.run(restTemplate).run();
        application.run(restTemplate).run();
    }
}
