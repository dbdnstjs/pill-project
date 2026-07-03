package com.pill.platform.domain.supplement.service;

import com.pill.platform.domain.supplement.client.FoodSafetyApiClient;
import com.pill.platform.domain.supplement.dto.SupplementIngredientInfo;
import com.pill.platform.domain.supplement.dto.SupplementResponse;
import com.pill.platform.domain.supplement.dto.SupplementSearchResult;
import com.pill.platform.domain.supplement.entity.Supplement;
import com.pill.platform.domain.supplement.repository.SupplementIngredientRepository;
import com.pill.platform.domain.supplement.repository.SupplementRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplementService {

  private final SupplementRepository supplementRepository;
  private final SupplementIngredientRepository supplementIngredientRepository;
  private final FoodSafetyApiClient foodSafetyApiClient;
  private final IngredientParseService ingredientParseService;

  public List<SupplementSearchResult> search(String keyword, int page, int size) {
    return foodSafetyApiClient.search(keyword, page, size);
  }

  @Transactional
  public SupplementResponse save(SupplementSearchResult result) {
    return supplementRepository
        .findByReportNo(result.reportNo())
        .map(existing -> {
          if (supplementIngredientRepository.findBySupplementId(existing.getId()).isEmpty()) {
            ingredientParseService.parseAndSave(existing);
          }
          return SupplementResponse.from(existing);
        })
        .orElseGet(
            () -> {
              Supplement supplement =
                  Supplement.builder()
                      .reportNo(result.reportNo())
                      .productName(result.productName())
                      .manufacturer(result.manufacturer())
                      .shape(result.shape())
                      .primaryFunction(result.primaryFunction())
                      .caution(result.caution())
                      .rawMaterial(result.rawMaterial())
                      .build();
              supplementRepository.save(supplement);
              ingredientParseService.parseAndSave(supplement);
              return SupplementResponse.from(supplement);
            });
  }

  public SupplementResponse getById(Long id) {
    return supplementRepository
        .findById(id)
        .map(SupplementResponse::from)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영양제입니다."));
  }

  public List<SupplementIngredientInfo> getIngredients(Long supplementId) {
    return supplementIngredientRepository.findBySupplementId(supplementId)
        .stream()
        .map(si -> new SupplementIngredientInfo(
            si.getIngredient().getName(), si.getAmount(), si.getUnit()))
        .toList();
  }
}
