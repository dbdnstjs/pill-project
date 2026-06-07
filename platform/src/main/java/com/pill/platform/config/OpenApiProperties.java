package com.pill.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = "openapi")
public class OpenApiProperties {

  private final String secretKey;

  @ConstructorBinding
  public OpenApiProperties(String secretKey) {
    this.secretKey = secretKey;
  }

  public String getSecretKey() {
    return secretKey;
  }
}
