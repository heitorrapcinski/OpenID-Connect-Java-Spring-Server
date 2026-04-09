package org.mitre.authserver.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // OAuth2 endpoints — client auth handled at application level
                .requestMatchers("/authorize").permitAll()
                .requestMatchers("/token").permitAll()
                .requestMatchers("/revoke").permitAll()
                .requestMatchers("/introspect").permitAll()
                .requestMatchers("/device_authorization").permitAll()
                // Actuator
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/**").authenticated()
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
