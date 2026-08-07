package com.mtl.qcvmt.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

  @Value("${qcvmt.cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
  private String allowedOrigins;

  @Value("${qcvmt.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
  private String allowedMethods;

  @Value("${qcvmt.cors.allowed-headers:*}")
  private String allowedHeaders;

  @Value("${qcvmt.cors.max-age:3600}")
  private long maxAge;

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration cors = new CorsConfiguration();
    cors.setAllowedOriginPatterns(split(allowedOrigins));
    cors.setAllowedMethods(split(allowedMethods));
    cors.setAllowedHeaders(split(allowedHeaders));
    cors.setAllowCredentials(true);
    cors.setMaxAge(maxAge);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", cors);
    return source;
  }

  private List<String> split(String csv) {
    return Arrays.stream(csv.split(","))
        .map(String::trim)
        .filter(s -> !s.isBlank())
        .toList();
  }
}
