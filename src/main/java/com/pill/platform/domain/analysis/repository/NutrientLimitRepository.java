package com.pill.platform.domain.analysis.repository;

import com.pill.platform.domain.analysis.entity.NutrientLimit;
import com.pill.platform.domain.user.entity.Gender;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NutrientLimitRepository extends JpaRepository<NutrientLimit, Long> {

  List<NutrientLimit> findByIngredientIdAndAgeGroupAndGender(
      Long ingredientId, String ageGroup, Gender gender);
}
