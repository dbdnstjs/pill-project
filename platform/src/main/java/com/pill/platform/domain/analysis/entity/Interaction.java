package com.pill.platform.domain.analysis.entity;

import com.pill.platform.domain.supplement.entity.Ingredient;
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
@Table(name = "interactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Interaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ingredient1_id", nullable = false)
  private Ingredient ingredient1;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ingredient2_id", nullable = false)
  private Ingredient ingredient2;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private InteractionStatus status;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(length = 200)
  private String source;

  @Builder
  public Interaction(
      Ingredient ingredient1,
      Ingredient ingredient2,
      InteractionStatus status,
      String description,
      String source) {
    this.ingredient1 = ingredient1;
    this.ingredient2 = ingredient2;
    this.status = status;
    this.description = description;
    this.source = source;
  }
}
