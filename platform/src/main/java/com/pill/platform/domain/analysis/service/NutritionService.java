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

    Map<String, Double> intakeMap = computeIntake(user);
    Map<String, String> unitMap = computeUnits(user);

    List<NutrientLimit> limits = nutrientLimitRepository.findByAgeGroupAndGender(ageGroup, gender);
    Map<String, NutrientLimit> limitMap = new LinkedHashMap<>();
    for (NutrientLimit l : limits) {
      limitMap.put(l.getIngredient().getName(), l);
    }

    List<NutrientItem> items = new ArrayList<>();
    for (Map.Entry<String, Double> entry : intakeMap.entrySet()) {
      String name = entry.getKey();
      double intake = entry.getValue();
      String unit = unitMap.get(name);
      NutrientLimit limit = limitMap.get(name);

      Double recommended = limit != null ? limit.getRecommendedAmount() : null;
      Double upperLimit = limit != null ? limit.getUpperLimit() : null;
      int pct =
          (recommended != null && recommended > 0)
              ? (int) Math.min((intake / recommended) * 100, 300)
              : 0;

      items.add(new NutrientItem(name, unit, intake, recommended, upperLimit, pct));
    }

    return new NutritionSummaryResponse(ageGroup, gender.name(), items);
  }

  /** 성분별 합산 섭취량 계산 */
  public Map<String, Double> computeIntake(User user) {
    Map<String, Double> intakeMap = new LinkedHashMap<>();
    for (UserSupplement us : userSupplementRepository.findByUserIdAndIsActiveTrue(user.getId())) {
      for (SupplementIngredient si :
          supplementIngredientRepository.findBySupplementId(us.getSupplement().getId())) {
        intakeMap.merge(si.getIngredient().getName(), si.getAmount(), Double::sum);
      }
    }
    return intakeMap;
  }

  private Map<String, String> computeUnits(User user) {
    Map<String, String> unitMap = new LinkedHashMap<>();
    for (UserSupplement us : userSupplementRepository.findByUserIdAndIsActiveTrue(user.getId())) {
      for (SupplementIngredient si :
          supplementIngredientRepository.findBySupplementId(us.getSupplement().getId())) {
        unitMap.putIfAbsent(si.getIngredient().getName(), si.getUnit());
      }
    }
    return unitMap;
  }

  private String computeAgeGroup(Integer birthYear) {
    if (birthYear == null) return "50-64";
    int age = LocalDate.now().getYear() - birthYear;
    if (age < 65) return "50-64";
    if (age < 75) return "65-74";
    return "75+";
  }
}
