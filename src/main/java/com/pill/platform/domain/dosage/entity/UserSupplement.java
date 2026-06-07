package com.pill.platform.domain.dosage.entity;

import com.pill.platform.domain.supplement.entity.Supplement;
import com.pill.platform.domain.user.entity.User;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "user_supplements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class UserSupplement {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "supplement_id", nullable = false)
  private Supplement supplement;

  private LocalDate startDate;

  private LocalDate endDate;

  @Column(nullable = false)
  private Boolean isActive = true;

  @CreatedDate
  @Column(updatable = false)
  private LocalDateTime createdAt;

  @Builder
  public UserSupplement(User user, Supplement supplement, LocalDate startDate, LocalDate endDate) {
    this.user = user;
    this.supplement = supplement;
    this.startDate = startDate;
    this.endDate = endDate;
    this.isActive = true;
  }

  public void deactivate() {
    this.isActive = false;
    this.endDate = LocalDate.now();
  }
}
