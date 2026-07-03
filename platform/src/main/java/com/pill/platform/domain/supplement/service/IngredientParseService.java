package com.pill.platform.domain.supplement.service;

import com.pill.platform.domain.supplement.entity.Supplement;
import com.pill.platform.domain.supplement.entity.SupplementIngredient;
import com.pill.platform.domain.supplement.repository.IngredientRepository;
import com.pill.platform.domain.supplement.repository.SupplementIngredientRepository;
import java.util.LinkedHashMap;
import java.util.Map;
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
      Pattern.compile("([\\d,]+(?:\\.\\d+)?)\\s*(mg|g|μg|mcg|IU|UI|㎎|㎍|㎰)", Pattern.CASE_INSENSITIVE);

  private static final Map<String, String> ALIASES = new LinkedHashMap<>();

  static {
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
    ALIASES.put("아연", "아연");
    ALIASES.put("황산철", "철분");
    ALIASES.put("철분", "철분");
    ALIASES.put("셀렌", "셀레늄");
    ALIASES.put("셀레늄", "셀레늄");
    ALIASES.put("요오드화칼륨", "요오드");
    ALIASES.put("요오드", "요오드");
    ALIASES.put("엽산", "엽산");
    ALIASES.put("나이아신", "나이아신");
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

    String[] segments = raw.split("[,;\\[\\]]");
    Map<String, double[]> aggregated = new LinkedHashMap<>();
    Map<String, String> unitAgg = new LinkedHashMap<>();
    Map<String, Boolean> nameOnly = new LinkedHashMap<>();

    for (String segment : segments) {
      String seg = segment.trim();
      if (seg.isBlank()) continue;

      Matcher matcher = AMOUNT_PATTERN.matcher(seg);
      if (matcher.find()) {
        double amount;
        try {
          amount = Double.parseDouble(matcher.group(1).replace(",", ""));
        } catch (NumberFormatException e) {
          continue;
        }
        String unit = normalizeUnit(matcher.group(2));
        String textBefore =
            seg.substring(0, matcher.start()).toLowerCase().replaceAll("[()（）]", " ").trim();
        String standardName = findStandardName(textBefore);
        if (standardName == null) continue;
        aggregated.merge(standardName, new double[]{amount}, (a, b) -> { a[0] += b[0]; return a; });
        unitAgg.putIfAbsent(standardName, unit);
        nameOnly.remove(standardName);
      } else {
        String text = seg.toLowerCase().replaceAll("[()（）]", " ").trim();
        String standardName = findStandardName(text);
        if (standardName != null && !aggregated.containsKey(standardName)) {
          nameOnly.put(standardName, true);
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

    for (String stdName : nameOnly.keySet()) {
      ingredientRepository.findByName(stdName).ifPresent(ingredient ->
          supplementIngredientRepository.save(
              SupplementIngredient.builder()
                  .supplement(supplement)
                  .ingredient(ingredient)
                  .amount(0.0)
                  .unit(null)
                  .build()));
    }
  }

  private String findStandardName(String text) {
    for (Map.Entry<String, String> entry : ALIASES.entrySet()) {
      if (text.contains(entry.getKey())) {
        return entry.getValue();
      }
    }
    return null;
  }

  private String normalizeUnit(String unit) {
    return switch (unit.toLowerCase()) {
      case "iu", "ui" -> "IU";
      case "mcg", "㎍" -> "μg";
      case "㎎" -> "mg";
      case "㎰" -> "g";
      default -> unit;
    };
  }
}
