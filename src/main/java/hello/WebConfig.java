package hello;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.UrlHandlerFilter;

@Configuration
public class WebConfig {

    /**
     * Spring Framework 6 no longer matches "/topic/" to "/topic". Keep the Boot 2 behaviour
     * by stripping the trailing slash before the request reaches the DispatcherServlet.
     */
    @Bean
    public FilterRegistrationBean<UrlHandlerFilter> trailingSlashFilter() {
        FilterRegistrationBean<UrlHandlerFilter> registration = new FilterRegistrationBean<>(
                UrlHandlerFilter.trailingSlashHandler("/**").wrapRequest().build());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
