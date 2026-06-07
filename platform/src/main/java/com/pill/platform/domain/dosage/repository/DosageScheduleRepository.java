package com.pill.platform.domain.dosage.repository;

import com.pill.platform.domain.dosage.entity.DosageSchedule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DosageScheduleRepository extends JpaRepository<DosageSchedule, Long> {

  List<DosageSchedule> findByUserSupplementIdAndIsActiveTrue(Long userSupplementId);
}
