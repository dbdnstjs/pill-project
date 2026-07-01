package com.pill.platform.domain.dosage.service;

import com.pill.platform.domain.dosage.dto.AutoScheduleResponse;
import com.pill.platform.domain.dosage.dto.DosageScheduleRequest;
import com.pill.platform.domain.dosage.dto.DosageScheduleResponse;
import com.pill.platform.domain.dosage.dto.UserSupplementRequest;
import com.pill.platform.domain.dosage.dto.UserSupplementResponse;
import com.pill.platform.domain.dosage.entity.DosageSchedule;
import com.pill.platform.domain.dosage.entity.UserSupplement;
import com.pill.platform.domain.dosage.repository.DosageScheduleRepository;
import com.pill.platform.domain.dosage.repository.UserSupplementRepository;
import com.pill.platform.domain.supplement.entity.Supplement;
import com.pill.platform.domain.supplement.repository.SupplementIngredientRepository;
import com.pill.platform.domain.supplement.repository.SupplementRepository;
import com.pill.platform.domain.user.entity.User;
import com.pill.platform.domain.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserSupplementService {

  private final UserSupplementRepository userSupplementRepository;
  private final DosageScheduleRepository dosageScheduleRepository;
  private final SupplementRepository supplementRepository;
  private final SupplementIngredientRepository supplementIngredientRepository;
  private final UserRepository userRepository;
  private final TimingRuleService timingRuleService;

  @Transactional
  public UserSupplementResponse register(String email, UserSupplementRequest request) {
    User user = getUser(email);
    Supplement supplement =
        supplementRepository
            .findById(request.supplementId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영양제입니다."));

    UserSupplement userSupplement =
        UserSupplement.builder()
            .user(user)
            .supplement(supplement)
            .startDate(request.startDate() != null ? request.startDate() : LocalDate.now())
            .build();

    return UserSupplementResponse.from(userSupplementRepository.save(userSupplement));
  }

  public List<UserSupplementResponse> getMySupplements(String email) {
    User user = getUser(email);
    return userSupplementRepository.findByUserIdAndIsActiveTrue(user.getId()).stream()
        .map(UserSupplementResponse::from)
        .toList();
  }

  @Transactional
  public void deactivate(String email, Long id) {
    UserSupplement us =
        userSupplementRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 복용 정보입니다."));
    if (!us.getUser().getId().equals(getUser(email).getId())) {
      throw new IllegalArgumentException("권한이 없습니다.");
    }
    us.deactivate();
  }

  @Transactional
  public DosageScheduleResponse addSchedule(
      String email, Long userSupplementId, DosageScheduleRequest request) {
    UserSupplement us = getMyUserSupplement(email, userSupplementId);
    DosageSchedule schedule =
        DosageSchedule.builder()
            .userSupplement(us)
            .scheduledTime(request.scheduledTime())
            .monday(request.monday())
            .tuesday(request.tuesday())
            .wednesday(request.wednesday())
            .thursday(request.thursday())
            .friday(request.friday())
            .saturday(request.saturday())
            .sunday(request.sunday())
            .build();
    return DosageScheduleResponse.from(dosageScheduleRepository.save(schedule));
  }

  public List<DosageScheduleResponse> getSchedules(String email, Long userSupplementId) {
    getMyUserSupplement(email, userSupplementId);
    return dosageScheduleRepository.findByUserSupplementIdAndIsActiveTrue(userSupplementId).stream()
        .map(DosageScheduleResponse::from)
        .toList();
  }

  @Transactional
  public void deleteSchedule(String email, Long userSupplementId, Long scheduleId) {
    getMyUserSupplement(email, userSupplementId);
    DosageSchedule schedule =
        dosageScheduleRepository
            .findById(scheduleId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스케줄입니다."));
    schedule.deactivate();
  }

  @Transactional
  public AutoScheduleResponse autoSchedule(String email) {
    User user = getUser(email);
    List<UserSupplement> activeSupplements =
        userSupplementRepository.findByUserIdAndIsActiveTrue(user.getId());

    Set<String> allIngredients = new LinkedHashSet<>();
    List<AutoScheduleResponse.Item> items = new ArrayList<>();

    for (UserSupplement us : activeSupplements) {
      List<String> ingredientNames =
          supplementIngredientRepository.findBySupplementId(us.getSupplement().getId()).stream()
              .map(si -> si.getIngredient().getName())
              .toList();
      allIngredients.addAll(ingredientNames);

      TimingRuleService.Bucket bucket = timingRuleService.resolveBucket(ingredientNames);

      dosageScheduleRepository
          .findByUserSupplementIdAndIsActiveTrue(us.getId())
          .forEach(DosageSchedule::deactivate);

      DosageSchedule schedule =
          DosageSchedule.builder()
              .userSupplement(us)
              .scheduledTime(bucket.getTime())
              .monday(true)
              .tuesday(true)
              .wednesday(true)
              .thursday(true)
              .friday(true)
              .saturday(true)
              .sunday(true)
              .build();
      dosageScheduleRepository.save(schedule);

      items.add(
          new AutoScheduleResponse.Item(
              us.getId(),
              us.getSupplement().getProductName(),
              bucket.getLabel(),
              bucket.getTime()));
    }

    String caution =
        allIngredients.contains("칼슘") && allIngredients.contains("철분")
            ? "철분과 칼슘은 서로 흡수를 방해해요. 칼슘은 아침에, 철분은 저녁에 드시도록 시간을 나눠 스케줄했어요."
            : null;

    return new AutoScheduleResponse(items, caution);
  }

  private UserSupplement getMyUserSupplement(String email, Long userSupplementId) {
    UserSupplement us =
        userSupplementRepository
            .findById(userSupplementId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 복용 정보입니다."));
    if (!us.getUser().getId().equals(getUser(email).getId())) {
      throw new IllegalArgumentException("권한이 없습니다.");
    }
    return us;
  }

  private User getUser(String email) {
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
  }
}
