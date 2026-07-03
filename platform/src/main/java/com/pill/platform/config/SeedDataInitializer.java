package com.pill.platform.config;

import com.pill.platform.domain.supplement.repository.IngredientRepository;
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

  @Override
  public void run(ApplicationArguments args) {
    if (ingredientRepository.count() > 0) {
      log.info("시드 데이터 이미 존재 ({} 건), 건너뜀", ingredientRepository.count());
      return;
    }
    log.info("시드 데이터 초기화 시작");
    ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
    populator.addScript(new ClassPathResource("data/seed_kdri2025.sql"));
    populator.setContinueOnError(false);
    populator.execute(dataSource);
    log.info("시드 데이터 초기화 완료 ({} 건)", ingredientRepository.count());
  }
}
