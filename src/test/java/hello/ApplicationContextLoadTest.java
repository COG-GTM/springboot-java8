package hello;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestClient;

import hello.controller.GreetingController;
import hello.controller.HelloController;
import hello.controller.TopicController;
import hello.service.TopicService;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Starts the whole application context, including the {@link org.springframework.boot.CommandLineRunner}
 * on {@link Application}. The runner contacts a demo quote service that no longer exists; the resulting
 * WARN must not fail startup, so reaching the assertions below is itself part of what is being verified.
 */
@SpringBootTest
class ApplicationContextLoadTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
        assertThat(context).isNotNull();
    }

    @Test
    void exposesTheExpectedBeans() {
        assertThat(context.getBean(TopicService.class)).isNotNull();
        assertThat(context.getBean(TopicController.class)).isNotNull();
        assertThat(context.getBean(HelloController.class)).isNotNull();
        assertThat(context.getBean(GreetingController.class)).isNotNull();
        assertThat(context.getBean(RestClient.class)).isNotNull();
        assertThat(context.getBean(JdbcTemplate.class)).isNotNull();
    }

    @Test
    void commandLineRunnerPopulatedTheCustomersTable() {
        Integer count = context.getBean(JdbcTemplate.class)
                .queryForObject("SELECT COUNT(*) FROM customers", Integer.class);
        assertThat(count).isEqualTo(4);
    }
}
