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

  /** 특정 영양제의 특정 성분 함량 업데이트 */
  @Modifying
  @Transactional
  @Query("UPDATE SupplementIngredient si SET si.amount = :amount, si.unit = :unit " +
         "WHERE si.supplement.id = :supplementId " +
         "AND si.ingredient.id IN " +
         "  (SELECT i.id FROM Ingredient i WHERE i.name = :ingredientName)")
  int updateAmountForIngredient(
      @Param("supplementId") Long supplementId,
      @Param("ingredientName") String ingredientName,
      @Param("amount") Double amount,
      @Param("unit") String unit);

  /** 사용자의 모든 활성 영양제에서 특정 성분 함량 업데이트 (영양소 페이지용) */
  @Modifying
  @Transactional
  @Query("UPDATE SupplementIngredient si SET si.amount = :amount, si.unit = :unit " +
         "WHERE si.supplement.id IN :supplementIds " +
         "AND si.ingredient.id IN " +
         "  (SELECT i.id FROM Ingredient i WHERE i.name = :ingredientName)")
  int updateAmountForIngredientAcrossSupplements(
      @Param("supplementIds") List<Long> supplementIds,
      @Param("ingredientName") String ingredientName,
      @Param("amount") Double amount,
      @Param("unit") String unit);
}
