package com.pill.platform.domain.dosage.service;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class TimingRuleService {

  public enum Bucket {
    FASTING(LocalTime.of(7, 0), "아침 공복"),
    MORNING_MEAL(LocalTime.of(8, 30), "아침 식후"),
    EVENING(LocalTime.of(21, 0), "저녁");

    private final LocalTime time;
    private final String label;

    Bucket(LocalTime time, String label) {
      this.time = time;
      this.label = label;
    }

    public LocalTime getTime() {
      return time;
    }

    public String getLabel() {
      return label;
    }
  }

  private static final Set<String> FASTING_INGREDIENTS = Set.of("유산균", "콜라겐");
  private static final Set<String> EVENING_INGREDIENTS = Set.of("철분", "마그네슘");

  public Bucket resolveBucket(List<String> ingredientNames) {
    if (ingredientNames.stream().anyMatch(FASTING_INGREDIENTS::contains)) {
      return Bucket.FASTING;
    }
    if (ingredientNames.stream().anyMatch(EVENING_INGREDIENTS::contains)) {
      return Bucket.EVENING;
    }
    return Bucket.MORNING_MEAL;
  }

  /** 시간대 라벨 조회 */
  public String labelFor(LocalTime scheduledTime) {
    for (Bucket bucket : Bucket.values()) {
      if (bucket.getTime().equals(scheduledTime)) {
        return bucket.getLabel();
      }
    }
    return "복용 시간";
  }
}
