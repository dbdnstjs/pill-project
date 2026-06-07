package com.pill.platform.domain.supplement.entity;

import com.pill.platform.common.entity.BaseEntity;
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
@Table(name = "supplements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Supplement extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 200)
  private String productName;

  @Column(length = 100)
  private String manufacturer;

  @Column(length = 100)
  private String category;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(length = 500)
  private String imageUrl;

  @Column(length = 100)
  private String apiProductId;

  @Builder
  public Supplement(
      String productName,
      String manufacturer,
      String category,
      String description,
      String imageUrl,
      String apiProductId) {
    this.productName = productName;
    this.manufacturer = manufacturer;
    this.category = category;
    this.description = description;
    this.imageUrl = imageUrl;
    this.apiProductId = apiProductId;
  }
}
