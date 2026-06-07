package com.pill.platform.domain.analysis.repository;

import com.pill.platform.domain.analysis.entity.Interaction;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {

  @Query(
      "SELECT i FROM Interaction i WHERE "
          + "(i.ingredient1.id = :id1 AND i.ingredient2.id = :id2) OR "
          + "(i.ingredient1.id = :id2 AND i.ingredient2.id = :id1)")
  Optional<Interaction> findByIngredientPair(
      @Param("id1") Long ingredientId1, @Param("id2") Long ingredientId2);

  @Query(
      "SELECT i FROM Interaction i WHERE " + "i.ingredient1.id IN :ids OR i.ingredient2.id IN :ids")
  List<Interaction> findAllByIngredientIds(@Param("ids") List<Long> ingredientIds);
}
