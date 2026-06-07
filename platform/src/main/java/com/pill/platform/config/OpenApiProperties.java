package com.pill.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = "openapi")
public class OpenApiProperties {

  private final String secretKey;
  private final String baseUrl;

  @ConstructorBinding
  public OpenApiProperties(String secretKey, String baseUrl) {
    this.secretKey = secretKey;
    this.baseUrl = baseUrl;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public String getBaseUrl() {
    return baseUrl;
  }
}
