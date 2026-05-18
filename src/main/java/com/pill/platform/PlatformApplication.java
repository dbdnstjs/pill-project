package com.pill.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class PlatformApplication {

  public static void main(String[] args) {
    SpringApplication.run(PlatformApplication.class, args);
  }
}
