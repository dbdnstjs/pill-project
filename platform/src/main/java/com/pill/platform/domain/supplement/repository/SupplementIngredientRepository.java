package com.pill.platform.domain.supplement.repository;

import com.pill.platform.domain.supplement.entity.SupplementIngredient;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface SupplementIngredientRepository extends JpaRepository<SupplementIngredient, Long> {

  List<SupplementIngredient> findBySupplementId(Long supplementId);

  /** 기존에 0.0으로 저장된 name-only 항목을 null로 마이그레이션 */
  @Modifying
  @Transactional
  @Query("UPDATE SupplementIngredient si SET si.amount = NULL WHERE si.amount = 0.0")
  int migrateZeroAmountsToNull();

  /** 사용자 영양제 목록에서 특정 성분의 amount가 null인 행을 일괄 업데이트 */
  @Modifying
  @Transactional
  @Query("UPDATE SupplementIngredient si SET si.amount = :amount, si.unit = :unit " +
         "WHERE si.supplement.id IN :supplementIds " +
         "AND si.ingredient.id IN " +
         "  (SELECT i.id FROM Ingredient i WHERE i.name = :ingredientName) " +
         "AND si.amount IS NULL")
  int updateAmountForIngredient(
      @Param("supplementIds") List<Long> supplementIds,
      @Param("ingredientName") String ingredientName,
      @Param("amount") Double amount,
      @Param("unit") String unit);
}
