package com.pill.platform.domain.supplement.repository;

import com.pill.platform.domain.supplement.entity.SupplementIngredient;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplementIngredientRepository extends JpaRepository<SupplementIngredient, Long> {

  List<SupplementIngredient> findBySupplementId(Long supplementId);
}
