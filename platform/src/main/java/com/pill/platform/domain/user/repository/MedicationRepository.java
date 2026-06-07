package com.pill.platform.domain.user.repository;

import com.pill.platform.domain.user.entity.Medication;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationRepository extends JpaRepository<Medication, Long> {

  List<Medication> findByUserId(Long userId);
}
