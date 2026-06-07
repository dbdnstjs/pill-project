package com.pill.platform.domain.user.entity;

import com.pill.platform.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 100)
  private String email;

  @Column(nullable = false)
  private String passwordHash;

  @Column(nullable = false, length = 50)
  private String name;

  private Integer birthYear;

  @Enumerated(EnumType.STRING)
  @Column(length = 10)
  private Gender gender;

  @Column(length = 20)
  private String ageGroup;

  @Builder
  public User(
      String email,
      String passwordHash,
      String name,
      Integer birthYear,
      Gender gender,
      String ageGroup) {
    this.email = email;
    this.passwordHash = passwordHash;
    this.name = name;
    this.birthYear = birthYear;
    this.gender = gender;
    this.ageGroup = ageGroup;
  }

  public void updatePassword(String passwordHash) {
    this.passwordHash = passwordHash;
  }
}
