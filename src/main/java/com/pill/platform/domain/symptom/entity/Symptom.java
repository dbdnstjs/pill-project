package com.pill.platform.domain.symptom.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "symptoms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Symptom {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(length = 50)
  private String category;

  @Column(length = 50)
  private String iconCode;

  @Builder
  public Symptom(String name, String category, String iconCode) {
    this.name = name;
    this.category = category;
    this.iconCode = iconCode;
  }
}
