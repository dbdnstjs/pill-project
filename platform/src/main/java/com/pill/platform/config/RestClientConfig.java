package com.pill.platform.config;

import java.time.Duration;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

  @Bean
  RestClient foodSafetyRestClient(OpenApiProperties properties) {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(Duration.ofSeconds(5));
    factory.setReadTimeout(Duration.ofSeconds(15));
    return RestClient.builder()
        .baseUrl(Objects.requireNonNull(properties.getBaseUrl(), "openapi.base-url must be set"))
        .requestFactory(factory)
        .build();
  }

  @Bean
  RestClient geminiRestClient() {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(Duration.ofSeconds(10));
    factory.setReadTimeout(Duration.ofSeconds(60));
    return RestClient.builder()
        .baseUrl("https://generativelanguage.googleapis.com/v1beta/models")
        .requestFactory(factory)
        .build();
  }
}
