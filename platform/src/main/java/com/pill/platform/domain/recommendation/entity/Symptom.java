package com.pill.platform.domain.recommendation.entity;

public enum Symptom {
  EYE_STRAIN("눈이 침침해요"),
  JOINT_PAIN("뼈 마디가 쑤셔요"),
  SLEEP("잠을 깊게 못 자요"),
  MEMORY("기억력이 떨어져요"),
  FATIGUE("피로하고 기운이 없어요"),
  BP_SUGAR("혈압/혈당이 걱정돼요");

  private final String label;

  Symptom(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
