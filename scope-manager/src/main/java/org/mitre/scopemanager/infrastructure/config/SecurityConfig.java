package org.mitre.scopemanager.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/scopes", "/scopes/defaults").permitAll()
                .requestMatchers(HttpMethod.POST, "/scopes").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/scopes/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/scopes/**").hasRole("ADMIN")
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/**").authenticated()
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> {});
        return http.build();
    }
}
