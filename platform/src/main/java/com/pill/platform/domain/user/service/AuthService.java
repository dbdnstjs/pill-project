package com.pill.platform.domain.user.service;

import com.pill.platform.domain.user.dto.AuthResponse;
import com.pill.platform.domain.user.dto.LoginRequest;
import com.pill.platform.domain.user.dto.SignUpRequest;
import com.pill.platform.domain.user.entity.User;
import com.pill.platform.domain.user.repository.UserRepository;
import com.pill.platform.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtProvider jwtProvider;

  public void signUp(SignUpRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
    }
    User user =
        User.builder()
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .name(request.getName())
            .birthYear(request.getBirthYear())
            .gender(request.getGender())
            .ageGroup(request.getAgeGroup())
            .build();
    userRepository.save(user);
  }

  @Transactional(readOnly = true)
  public AuthResponse login(LoginRequest request) {
    User user =
        userRepository
            .findByEmail(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    String token = jwtProvider.generateToken(user.getEmail());
    return new AuthResponse(token, user.getName());
  }
}
