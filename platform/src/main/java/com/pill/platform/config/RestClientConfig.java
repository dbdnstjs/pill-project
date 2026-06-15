package com.pill.platform.config;

import java.util.Objects;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

  @Bean
  RestClient foodSafetyRestClient(OpenApiProperties properties) {
    return RestClient.builder()
        .baseUrl(Objects.requireNonNull(properties.getBaseUrl(), "openapi.base-url must be set"))
        .build();
  }
}
