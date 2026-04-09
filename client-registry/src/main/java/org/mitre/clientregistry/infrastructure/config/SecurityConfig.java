package org.mitre.clientregistry.infrastructure.config;

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
                .requestMatchers(HttpMethod.POST, "/register").permitAll()
                .requestMatchers(HttpMethod.GET, "/register/**")
                    .access((authentication, context) ->
                        new org.springframework.security.authorization.AuthorizationDecision(
                            authentication.get().getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("SCOPE_registration")
                                        || a.getAuthority().equals("ROLE_ADMIN"))
                        ))
                .requestMatchers(HttpMethod.PUT, "/register/**")
                    .access((authentication, context) ->
                        new org.springframework.security.authorization.AuthorizationDecision(
                            authentication.get().getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("SCOPE_registration")
                                        || a.getAuthority().equals("ROLE_ADMIN"))
                        ))
                .requestMatchers(HttpMethod.DELETE, "/register/**")
                    .access((authentication, context) ->
                        new org.springframework.security.authorization.AuthorizationDecision(
                            authentication.get().getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("SCOPE_registration")
                                        || a.getAuthority().equals("ROLE_ADMIN"))
                        ))
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/**").authenticated()
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> {});
        return http.build();
    }
}
