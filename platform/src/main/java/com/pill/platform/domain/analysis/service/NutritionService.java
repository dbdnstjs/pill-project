package com.pill.platform.domain.analysis.service;

import com.pill.platform.domain.analysis.dto.NutritionSummaryResponse;
import com.pill.platform.domain.analysis.dto.NutritionSummaryResponse.NutrientItem;
import com.pill.platform.domain.analysis.entity.NutrientLimit;
import com.pill.platform.domain.analysis.repository.NutrientLimitRepository;
import com.pill.platform.domain.dosage.entity.UserSupplement;
import com.pill.platform.domain.dosage.repository.UserSupplementRepository;
import com.pill.platform.domain.supplement.entity.SupplementIngredient;
import com.pill.platform.domain.supplement.repository.SupplementIngredientRepository;
import com.pill.platform.domain.user.entity.Gender;
import com.pill.platform.domain.user.entity.User;
import com.pill.platform.domain.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NutritionService {

  private final UserRepository userRepository;
  private final UserSupplementRepository userSupplementRepository;
  private final SupplementIngredientRepository supplementIngredientRepository;
  private final NutrientLimitRepository nutrientLimitRepository;

  public NutritionSummaryResponse getSummary(String email) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    String ageGroup = computeAgeGroup(user.getBirthYear());
    Gender gender = user.getGender() != null ? user.getGender() : Gender.MALE;

    Map<String, Double> intakeMap = new LinkedHashMap<>();
    Map<String, String> unitMap = new LinkedHashMap<>();
    Map<String, Boolean> hasAmountMap = new LinkedHashMap<>();

    for (UserSupplement us : userSupplementRepository.findByUserIdAndIsActiveTrue(user.getId())) {
      for (SupplementIngredient si :
          supplementIngredientRepository.findBySupplementId(us.getSupplement().getId())) {
        String name = si.getIngredient().getName();
        unitMap.putIfAbsent(name, si.getUnit());
        if (si.getAmount() != null) {
          intakeMap.merge(name, si.getAmount(), (a, b) -> a + b);
          hasAmountMap.put(name, true);
        } else {
          hasAmountMap.putIfAbsent(name, false);
        }
      }
    }

    List<NutrientLimit> limits = nutrientLimitRepository.findByAgeGroupAndGender(ageGroup, gender);
    Map<String, NutrientLimit> limitMap = new LinkedHashMap<>();
    for (NutrientLimit l : limits) {
      limitMap.put(l.getIngredient().getName(), l);
    }

    List<NutrientItem> items = new ArrayList<>();
    for (String name : hasAmountMap.keySet()) {
      boolean hasAmount = hasAmountMap.get(name);
      double intake = intakeMap.getOrDefault(name, 0.0);
      String unit = unitMap.get(name);
      NutrientLimit limit = limitMap.get(name);

      Double recommended = limit != null ? limit.getRecommendedAmount() : null;
      Double upperLimit = limit != null ? limit.getUpperLimit() : null;
      int pct =
          (hasAmount && recommended != null && recommended > 0)
              ? (int) Math.min((intake / recommended) * 100, 300)
              : 0;

      items.add(new NutrientItem(name, unit, intake, recommended, upperLimit, pct, hasAmount));
    }

    return new NutritionSummaryResponse(ageGroup, gender.name(), items);
  }

  /** 성분별 합산 섭취량 계산 (amount null 은 0으로 처리) */
  public Map<String, Double> computeIntake(User user) {
    Map<String, Double> intakeMap = new LinkedHashMap<>();
    for (UserSupplement us : userSupplementRepository.findByUserIdAndIsActiveTrue(user.getId())) {
      for (SupplementIngredient si :
          supplementIngredientRepository.findBySupplementId(us.getSupplement().getId())) {
        if (si.getAmount() != null) {
          intakeMap.merge(si.getIngredient().getName(), si.getAmount(), (a, b) -> a + b);
        }
      }
    }
    return intakeMap;
  }

  @Transactional
  public void updateIngredientAmount(String email, String ingredientName, double amount, String unit) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    List<Long> supplementIds =
        userSupplementRepository.findByUserIdAndIsActiveTrue(user.getId()).stream()
            .map(us -> us.getSupplement().getId())
            .toList();

    if (supplementIds.isEmpty()) return;

    supplementIngredientRepository.updateAmountForIngredient(
        supplementIds, ingredientName, amount, unit);
  }

  private String computeAgeGroup(Integer birthYear) {
    if (birthYear == null) return "30-49";
    int age = LocalDate.now().getYear() - birthYear;
    if (age < 30) return "19-29";
    if (age < 50) return "30-49";
    if (age < 65) return "50-64";
    if (age < 75) return "65-74";
    return "75+";
  }
}
