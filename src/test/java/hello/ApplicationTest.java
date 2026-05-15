package hello;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
public class ApplicationTest {

    @MockBean
    private RestTemplate restTemplate;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public CommandLineRunner run(RestTemplate restTemplate) {
            return args -> {};
        }
    }

    @Test
    public void contextLoads() {
    }
}
