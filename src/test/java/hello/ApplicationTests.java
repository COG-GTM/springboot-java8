package hello;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ApplicationTests {

    private final ApplicationContext context;

    ApplicationTests(ApplicationContext context) {
        this.context = context;
    }

    @Test
    void contextLoads() {
        assertThat(context.containsBean("topicController")).isTrue();
        assertThat(context.containsBean("helloController")).isTrue();
        assertThat(context.containsBean("greetingController")).isTrue();
    }

    @Test
    void quoteRunnerIsDisabledByDefault() {
        assertThat(context.containsBean("quoteRunner")).isFalse();
    }
}
