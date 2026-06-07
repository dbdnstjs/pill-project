package com.pill.platform.domain.dosage.repository;

import com.pill.platform.domain.dosage.entity.DosageRecord;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DosageRecordRepository extends JpaRepository<DosageRecord, Long> {

  List<DosageRecord> findByUserSupplementIdAndTakenAtBetween(
      Long userSupplementId, LocalDateTime from, LocalDateTime to);
}
