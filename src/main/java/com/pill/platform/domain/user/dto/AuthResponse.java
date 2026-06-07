package com.pill.platform.domain.user.dto;

import lombok.Getter;

@Getter
public class AuthResponse {

  private final String accessToken;
  private final String name;

  public AuthResponse(String accessToken, String name) {
    this.accessToken = accessToken;
    this.name = name;
  }
}
