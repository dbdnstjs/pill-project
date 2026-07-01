package com.pill.platform.domain.recommendation.service;

import com.pill.platform.domain.analysis.entity.NutrientLimit;
import com.pill.platform.domain.analysis.repository.NutrientLimitRepository;
import com.pill.platform.domain.analysis.service.NutritionService;
import com.pill.platform.domain.recommendation.dto.RecommendationResponse;
import com.pill.platform.domain.recommendation.dto.RecommendationResponse.Item;
import com.pill.platform.domain.recommendation.entity.Symptom;
import com.pill.platform.domain.user.entity.Gender;
import com.pill.platform.domain.user.entity.User;
import com.pill.platform.domain.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SymptomRecommendationService {

  private final UserRepository userRepository;
  private final NutritionService nutritionService;
  private final NutrientLimitRepository nutrientLimitRepository;

  private record Recommendation(String ingredientName, String reason) {}

  private static final Map<Symptom, List<Recommendation>> RULES = new EnumMap<>(Symptom.class);

  static {
    RULES.put(
        Symptom.EYE_STRAIN,
        List.of(
            new Recommendation("루테인+지아잔틴", "눈의 황반 건강을 지켜줘요."),
            new Recommendation("비타민 A", "시력 유지에 도움을 줘요.")));
    RULES.put(
        Symptom.JOINT_PAIN,
        List.of(
            new Recommendation("칼슘", "뼈와 관절 건강의 기본이 되는 영양소예요."),
            new Recommendation("비타민 D", "칼슘이 몸에 잘 흡수되도록 도와줘요.")));
    RULES.put(Symptom.SLEEP, List.of(new Recommendation("마그네슘", "신경을 안정시켜 깊은 잠을 자는 데 도움을 줘요.")));
    RULES.put(Symptom.MEMORY, List.of(new Recommendation("오메가3", "두뇌 활동(DHA)에 필요한 영양소예요.")));
    RULES.put(
        Symptom.FATIGUE,
        List.of(
            new Recommendation("비타민 B1", "에너지를 만드는 대사 과정에 필요해요."),
            new Recommendation("비타민 B2", "에너지를 만드는 대사 과정에 필요해요."),
            new Recommendation("비타민 B12", "신경과 에너지 대사에 도움을 줘요."),
            new Recommendation("마그네슘", "근육 피로 회복에 도움을 줘요.")));
    RULES.put(
        Symptom.BP_SUGAR,
        List.of(
            new Recommendation("오메가3", "혈행 개선에 도움을 줘요."),
            new Recommendation("코엔자임Q10", "심혈관 건강에 도움을 줘요. 혈압/혈당 약을 드시고 계시면 담당 의사와 꼭 상의하세요.")));
  }

  public RecommendationResponse recommend(String email, Symptom symptom) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    Map<String, Double> intake = nutritionService.computeIntake(user);
    Map<String, NutrientLimit> limitMap = limitMapFor(user);

    List<Item> items =
        RULES.getOrDefault(symptom, List.of()).stream()
            .map(
                r -> {
                  double current = intake.getOrDefault(r.ingredientName(), 0.0);
                  NutrientLimit limit = limitMap.get(r.ingredientName());
                  boolean sufficient =
                      limit != null
                          && limit.getRecommendedAmount() != null
                          && current >= limit.getRecommendedAmount();
                  return new Item(r.ingredientName(), r.reason(), sufficient);
                })
            .toList();

    return new RecommendationResponse(symptom.name(), symptom.getLabel(), items);
  }

  private Map<String, NutrientLimit> limitMapFor(User user) {
    String ageGroup = computeAgeGroup(user.getBirthYear());
    Gender gender = user.getGender() != null ? user.getGender() : Gender.MALE;
    Map<String, NutrientLimit> map = new java.util.LinkedHashMap<>();
    for (NutrientLimit l : nutrientLimitRepository.findByAgeGroupAndGender(ageGroup, gender)) {
      map.put(l.getIngredient().getName(), l);
    }
    return map;
  }

  private String computeAgeGroup(Integer birthYear) {
    if (birthYear == null) return "50-64";
    int age = LocalDate.now().getYear() - birthYear;
    if (age < 65) return "50-64";
    if (age < 75) return "65-74";
    return "75+";
  }
}
