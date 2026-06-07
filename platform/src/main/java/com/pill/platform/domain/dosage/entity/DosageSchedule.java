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
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "dosage_schedules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class DosageSchedule {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_supplement_id", nullable = false)
  private UserSupplement userSupplement;

  @Column(nullable = false)
  private LocalTime scheduledTime;

  @Column(nullable = false)
  private Boolean monday = false;

  @Column(nullable = false)
  private Boolean tuesday = false;

  @Column(nullable = false)
  private Boolean wednesday = false;

  @Column(nullable = false)
  private Boolean thursday = false;

  @Column(nullable = false)
  private Boolean friday = false;

  @Column(nullable = false)
  private Boolean saturday = false;

  @Column(nullable = false)
  private Boolean sunday = false;

  @Column(nullable = false)
  private Boolean isActive = true;

  @CreatedDate
  @Column(updatable = false)
  private LocalDateTime createdAt;

  @Builder
  public DosageSchedule(
      UserSupplement userSupplement,
      LocalTime scheduledTime,
      Boolean monday,
      Boolean tuesday,
      Boolean wednesday,
      Boolean thursday,
      Boolean friday,
      Boolean saturday,
      Boolean sunday) {
    this.userSupplement = userSupplement;
    this.scheduledTime = scheduledTime;
    this.monday = monday != null && monday;
    this.tuesday = tuesday != null && tuesday;
    this.wednesday = wednesday != null && wednesday;
    this.thursday = thursday != null && thursday;
    this.friday = friday != null && friday;
    this.saturday = saturday != null && saturday;
    this.sunday = sunday != null && sunday;
    this.isActive = true;
  }

  public void deactivate() {
    this.isActive = false;
  }
}
