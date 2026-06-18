package hello.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable() // Disable CSRF for REST API (stateless); re-enable if serving browser forms
            .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/topic", "/topic/**").permitAll()
                .antMatchers(HttpMethod.POST, "/topic").authenticated()
                .antMatchers(HttpMethod.PUT, "/topic/**").authenticated()
                .antMatchers(HttpMethod.DELETE, "/topic/**").authenticated()
                .antMatchers("/h2-console/**").denyAll()
                .anyRequest().authenticated()
            .and()
            .httpBasic();

        // Allow H2 console frames to be blocked
        http.headers().frameOptions().deny();

        return http.build();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails user = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("changeme"))
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
