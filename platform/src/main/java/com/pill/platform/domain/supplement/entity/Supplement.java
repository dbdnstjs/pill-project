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

  @Column(length = 50, unique = true)
  private String reportNo;

  @Column(nullable = false, length = 200)
  private String productName;

  @Column(length = 200)
  private String manufacturer;

  @Column(length = 50)
  private String shape;

  @Column(columnDefinition = "TEXT")
  private String primaryFunction;

  @Column(columnDefinition = "TEXT")
  private String caution;

  @Column(columnDefinition = "TEXT")
  private String rawMaterial;

  @Column(columnDefinition = "TEXT")
  private String dosageMethod;

  @Column(length = 500)
  private String imageUrl;

  @Builder
  public Supplement(
      String reportNo,
      String productName,
      String manufacturer,
      String shape,
      String primaryFunction,
      String caution,
      String rawMaterial,
      String dosageMethod,
      String imageUrl) {
    this.reportNo = reportNo;
    this.productName = productName;
    this.manufacturer = manufacturer;
    this.shape = shape;
    this.primaryFunction = primaryFunction;
    this.caution = caution;
    this.rawMaterial = rawMaterial;
    this.dosageMethod = dosageMethod;
    this.imageUrl = imageUrl;
  }
}
