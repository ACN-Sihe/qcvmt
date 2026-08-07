package com.mtl.qcvmt.config;

import com.mtl.qcvmt.security.JwtAuthConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/users/**", "/api/operation-logs/**", "/api/import/**", "/api/export/**")
                        .hasRole("qcvmt-admin")
                        .requestMatchers("/api/vessels/**", "/api/color-sets/**", "/api/vessel-colors/**",
                                "/api/vessel-refuels/**", "/api/bay-config/**")
                        .hasRole("qcvmt-admin")
                        .requestMatchers("/api/terminal/**")
                        .hasAnyRole("qcvmt-admin", "qcvmt-user")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(
                        oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(new JwtAuthConverter())))
                .cors(Customizer.withDefaults());

        return http.build();
    }
}
