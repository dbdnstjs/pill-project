package com.pill.platform.domain.symptom.repository;

import com.pill.platform.domain.symptom.entity.Symptom;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SymptomRepository extends JpaRepository<Symptom, Long> {

  List<Symptom> findByCategory(String category);
}
