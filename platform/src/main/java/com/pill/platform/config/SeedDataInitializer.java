package com.pill.platform.config;

import com.pill.platform.domain.supplement.entity.Supplement;
import com.pill.platform.domain.supplement.repository.IngredientRepository;
import com.pill.platform.domain.supplement.repository.SupplementIngredientRepository;
import com.pill.platform.domain.supplement.repository.SupplementRepository;
import com.pill.platform.domain.supplement.service.IngredientParseService;
import java.util.List;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeedDataInitializer implements ApplicationRunner {

  private final DataSource dataSource;
  private final IngredientRepository ingredientRepository;
  private final SupplementRepository supplementRepository;
  private final SupplementIngredientRepository supplementIngredientRepository;
  private final IngredientParseService ingredientParseService;

  @Override
  public void run(ApplicationArguments args) {
    migrateSchema();
    if (ingredientRepository.count() == 0) {
      log.info("시드 데이터 초기화 시작");
      ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
      populator.addScript(new ClassPathResource("data/seed_kdri2025.sql"));
      populator.setContinueOnError(false);
      populator.execute(dataSource);
      log.info("시드 데이터 초기화 완료 ({} 건)", ingredientRepository.count());
    } else {
      log.info("시드 데이터 이미 존재 ({} 건)", ingredientRepository.count());
    }

    migrateZeroAmounts();
    reparseMissingIngredients();
  }

  private void migrateSchema() {
    try (java.sql.Connection conn = dataSource.getConnection();
         java.sql.Statement stmt = conn.createStatement()) {
      stmt.execute(
          "ALTER TABLE supplement_ingredients ALTER COLUMN amount DROP NOT NULL");
      log.info("스키마 마이그레이션: supplement_ingredients.amount NOT NULL 제약 제거");
    } catch (Exception e) {
      log.debug("amount 컬럼 마이그레이션 건너뜀 (이미 nullable): {}", e.getMessage());
    }
  }

  private void migrateZeroAmounts() {
    int updated = supplementIngredientRepository.migrateZeroAmountsToNull();
    if (updated > 0) {
      log.info("amount=0.0 → null 마이그레이션 완료: {} 건", updated);
    }
  }

  private void reparseMissingIngredients() {
    List<Supplement> supplements = supplementRepository.findAll();
    int count = 0;
    for (Supplement supplement : supplements) {
      if (supplementIngredientRepository.findBySupplementId(supplement.getId()).isEmpty()) {
        ingredientParseService.parseAndSave(supplement);
        count++;
      }
    }
    if (count > 0) {
      log.info("성분 재파싱 완료: {} 건", count);
    }
  }
}
