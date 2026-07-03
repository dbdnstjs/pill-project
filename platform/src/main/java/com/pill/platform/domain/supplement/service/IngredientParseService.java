package com.pill.platform.domain.supplement.service;

import com.pill.platform.domain.supplement.entity.Supplement;
import com.pill.platform.domain.supplement.entity.SupplementIngredient;
import com.pill.platform.domain.supplement.repository.IngredientRepository;
import com.pill.platform.domain.supplement.repository.SupplementIngredientRepository;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngredientParseService {

  private final IngredientRepository ingredientRepository;
  private final SupplementIngredientRepository supplementIngredientRepository;

  private static final Pattern AMOUNT_PATTERN =
      Pattern.compile("([\\d,]+(?:\\.\\d+)?)\\s*(mg|g|μg|mcg|IU|UI|㎎|㎍)", Pattern.CASE_INSENSITIVE);

  private static final Map<String, String> ALIASES = new LinkedHashMap<>();

  static {
    // 더 구체적인 별칭을 먼저 등록 (같은 위치 충돌 시 먼저 등록된 것이 우선)
    ALIASES.put("비타민b12", "비타민 B12");
    ALIASES.put("코발라민", "비타민 B12");
    ALIASES.put("비타민b6", "비타민 B6");
    ALIASES.put("피리독신", "비타민 B6");
    ALIASES.put("비타민b2", "비타민 B2");
    ALIASES.put("리보플라빈", "비타민 B2");
    ALIASES.put("비타민b1", "비타민 B1");
    ALIASES.put("티아민", "비타민 B1");
    ALIASES.put("비타민c", "비타민 C");
    ALIASES.put("아스코르브산", "비타민 C");
    ALIASES.put("콜레칼시페롤", "비타민 D");
    ALIASES.put("비타민d3", "비타민 D");
    ALIASES.put("비타민d", "비타민 D");
    ALIASES.put("레티놀", "비타민 A");
    ALIASES.put("비타민a", "비타민 A");
    ALIASES.put("토코페롤", "비타민 E");
    ALIASES.put("비타민e", "비타민 E");
    ALIASES.put("비타민k2", "비타민 K");
    ALIASES.put("비타민k1", "비타민 K");
    ALIASES.put("비타민k", "비타민 K");
    ALIASES.put("유청칼슘", "칼슘");
    ALIASES.put("탄산칼슘", "칼슘");
    ALIASES.put("구연산칼슘", "칼슘");
    ALIASES.put("칼슘", "칼슘");
    ALIASES.put("산화마그네슘", "마그네슘");
    ALIASES.put("구연산마그네슘", "마그네슘");
    ALIASES.put("마그네슘", "마그네슘");
    ALIASES.put("오메가3", "오메가3");
    ALIASES.put("dha", "오메가3");
    ALIASES.put("epa", "오메가3");
    ALIASES.put("어유", "오메가3");
    ALIASES.put("황산아연", "아연");
    ALIASES.put("글루콘산아연", "아연");
    ALIASES.put("아연", "아연");
    ALIASES.put("황산철", "철분");
    ALIASES.put("푸마르산철", "철분");
    ALIASES.put("철분", "철분");
    ALIASES.put("셀렌", "셀레늄");
    ALIASES.put("셀레늄", "셀레늄");
    ALIASES.put("요오드화칼륨", "요오드");
    ALIASES.put("요오드", "요오드");
    ALIASES.put("엽산", "엽산");
    ALIASES.put("나이아신", "나이아신");
    ALIASES.put("니코틴산", "나이아신");
    ALIASES.put("루테인", "루테인+지아잔틴");
    ALIASES.put("지아잔틴", "루테인+지아잔틴");
    ALIASES.put("코엔자임q10", "코엔자임Q10");
    ALIASES.put("코큐텐", "코엔자임Q10");
    ALIASES.put("ubiquinone", "코엔자임Q10");
  }

  @Transactional
  public void parseAndSave(Supplement supplement) {
    String raw = supplement.getRawMaterial();
    if (raw == null || raw.isBlank()) return;

    // 대괄호만 제거 — 쉼표·소괄호는 유지 (괄호 안 쉼표 문제 방지)
    String text = raw.replaceAll("[\\[\\]]", " ");

    Map<String, double[]> aggregated = new LinkedHashMap<>();
    Map<String, String> unitAgg = new LinkedHashMap<>();

    // Pass 1: 텍스트 전체에서 숫자+단위 스캔, 각 위치 왼쪽에서 가장 가까운 성분명 탐색
    Matcher m = AMOUNT_PATTERN.matcher(text);
    while (m.find()) {
      double amount;
      try {
        amount = Double.parseDouble(m.group(1).replace(",", ""));
      } catch (NumberFormatException e) {
        continue;
      }
      String unit = normalizeUnit(m.group(2));

      // 숫자 앞 최대 80자 컨텍스트에서 가장 오른쪽(가까운) 성분명 찾기
      String context = text.substring(Math.max(0, m.start() - 80), m.start());
      String standardName = findNearestStandardName(context);
      if (standardName == null) continue;

      aggregated.merge(standardName, new double[]{amount}, (a, b) -> {
        a[0] += b[0];
        return a;
      });
      unitAgg.putIfAbsent(standardName, unit);
    }

    // Pass 2: 텍스트에 이름은 있지만 함량을 못 찾은 성분 (name-only)
    String normFull = normalizeForMatch(text);
    Set<String> nameOnly = new LinkedHashSet<>();
    for (Map.Entry<String, String> entry : ALIASES.entrySet()) {
      if (normFull.contains(entry.getKey())) {
        String stdName = entry.getValue();
        if (!aggregated.containsKey(stdName)) {
          nameOnly.add(stdName);
        }
      }
    }

    for (Map.Entry<String, double[]> entry : aggregated.entrySet()) {
      String stdName = entry.getKey();
      ingredientRepository.findByName(stdName).ifPresent(ingredient ->
          supplementIngredientRepository.save(
              SupplementIngredient.builder()
                  .supplement(supplement)
                  .ingredient(ingredient)
                  .amount(entry.getValue()[0])
                  .unit(unitAgg.get(stdName))
                  .build()));
    }

    for (String stdName : nameOnly) {
      ingredientRepository.findByName(stdName).ifPresent(ingredient ->
          supplementIngredientRepository.save(
              SupplementIngredient.builder()
                  .supplement(supplement)
                  .ingredient(ingredient)
                  .amount(null)
                  .unit(null)
                  .build()));
    }
  }

  /**
   * 컨텍스트에서 lastIndexOf로 가장 오른쪽(숫자에 가장 가까운) 성분 별칭을 찾아 반환.
   * 같은 위치에 여러 별칭이 겹치면 ALIASES 삽입 순서상 앞선 것(더 구체적인 것)이 우선.
   */
  private String findNearestStandardName(String context) {
    String normalized = normalizeForMatch(context);
    int bestPos = -1;
    String bestName = null;
    for (Map.Entry<String, String> entry : ALIASES.entrySet()) {
      int pos = normalized.lastIndexOf(entry.getKey());
      if (pos > bestPos) {
        bestPos = pos;
        bestName = entry.getValue();
      }
    }
    return bestName;
  }

  /**
   * 매칭용 정규화:
   * - 소문자 변환
   * - L-, D-, DL- 입체화학 접두어 제거 (L-아스코르브산 → 아스코르브산)
   * - 한글·영소문자·숫자 외 모든 문자 제거 (공백, 괄호, 쉼표 포함)
   * → "비타민 B12"와 "비타민B12"가 동일하게 처리됨
   */
  private String normalizeForMatch(String text) {
    return text.toLowerCase()
        .replaceAll("\\b(?:dl?|l)-", "")
        .replaceAll("[^가-힣a-z0-9]", "");
  }

  private String normalizeUnit(String unit) {
    return switch (unit.toLowerCase()) {
      case "iu", "ui" -> "IU";
      case "mcg", "㎍" -> "μg";
      case "㎎" -> "mg";
      default -> unit;
    };
  }
}
