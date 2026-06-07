package com.pill.platform.domain.user.dto;

import com.pill.platform.domain.user.entity.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignUpRequest {

  @NotBlank @Email private String email;

  @NotBlank
  @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
  private String password;

  @NotBlank private String name;

  private Integer birthYear;

  private Gender gender;

  private String ageGroup;
}
