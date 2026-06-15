package com.pill.platform.domain.dosage.repository;

import com.pill.platform.domain.dosage.entity.DosageRecord;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DosageRecordRepository extends JpaRepository<DosageRecord, Long> {

  List<DosageRecord> findByUserSupplementIdAndTakenAtBetween(
      Long userSupplementId, LocalDateTime from, LocalDateTime to);

  @Query(
      "SELECT dr FROM DosageRecord dr"
          + " WHERE dr.userSupplement.user.id = :userId"
          + " AND dr.takenAt BETWEEN :from AND :to")
  List<DosageRecord> findByUserIdAndTakenAtBetween(
      @Param("userId") Long userId,
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to);
}
