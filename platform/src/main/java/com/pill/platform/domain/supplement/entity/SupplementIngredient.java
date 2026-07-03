package com.pill.platform.domain.supplement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "supplement_ingredients")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SupplementIngredient {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "supplement_id", nullable = false)
  private Supplement supplement;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ingredient_id", nullable = false)
  private Ingredient ingredient;

  @Column(nullable = true)
  private Double amount;

  @Column(length = 20)
  private String unit;

  @Builder
  public SupplementIngredient(
      Supplement supplement, Ingredient ingredient, Double amount, String unit) {
    this.supplement = supplement;
    this.ingredient = ingredient;
    this.amount = amount;
    this.unit = unit;
  }
}
