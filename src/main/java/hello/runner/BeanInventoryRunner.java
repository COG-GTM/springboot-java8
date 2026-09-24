package hello.runner;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class BeanInventoryRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(BeanInventoryRunner.class);

    private final ApplicationContext context;

    public BeanInventoryRunner(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void run(String... args) {
        log.info("Let's inspect the beans provided by Spring Boot:");

        String[] beanNames = context.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        for (String beanName : beanNames) {
            log.info(beanName);
        }
    }
}
