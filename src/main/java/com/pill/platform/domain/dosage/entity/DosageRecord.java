package com.pill.platform.domain.dosage.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "dosage_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class DosageRecord {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_supplement_id", nullable = false)
  private UserSupplement userSupplement;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "dosage_schedule_id")
  private DosageSchedule dosageSchedule;

  @Column(nullable = false)
  private LocalDateTime takenAt;

  @Column(nullable = false)
  private Boolean isTaken;

  @Column(columnDefinition = "TEXT")
  private String note;

  @CreatedDate
  @Column(updatable = false)
  private LocalDateTime createdAt;

  @Builder
  public DosageRecord(
      UserSupplement userSupplement,
      DosageSchedule dosageSchedule,
      LocalDateTime takenAt,
      Boolean isTaken,
      String note) {
    this.userSupplement = userSupplement;
    this.dosageSchedule = dosageSchedule;
    this.takenAt = takenAt;
    this.isTaken = isTaken;
    this.note = note;
  }
}
