package com.pill.platform.domain.analysis.entity;

import com.pill.platform.domain.supplement.entity.Ingredient;
import com.pill.platform.domain.user.entity.Gender;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "nutrient_limits")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NutrientLimit {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ingredient_id", nullable = false)
  private Ingredient ingredient;

  @Column(nullable = false, length = 20)
  private String ageGroup;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private Gender gender;

  private Double recommendedAmount;

  private Double upperLimit;

  @Column(nullable = false, length = 20)
  private String unit;

  @Builder
  public NutrientLimit(
      Ingredient ingredient,
      String ageGroup,
      Gender gender,
      Double recommendedAmount,
      Double upperLimit,
      String unit) {
    this.ingredient = ingredient;
    this.ageGroup = ageGroup;
    this.gender = gender;
    this.recommendedAmount = recommendedAmount;
    this.upperLimit = upperLimit;
    this.unit = unit;
  }
}
