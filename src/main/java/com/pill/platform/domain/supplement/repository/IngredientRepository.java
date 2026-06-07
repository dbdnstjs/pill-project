package com.pill.platform.domain.supplement.repository;

import com.pill.platform.domain.supplement.entity.Ingredient;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

  Optional<Ingredient> findByName(String name);
}
