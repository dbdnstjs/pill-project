package com.pill.platform.domain.dosage.service;

import com.pill.platform.domain.dosage.dto.DosageRecordRequest;
import com.pill.platform.domain.dosage.dto.DosageRecordResponse;
import com.pill.platform.domain.dosage.dto.TodayChecklistResponse;
import com.pill.platform.domain.dosage.dto.TodayChecklistResponse.Item;
import com.pill.platform.domain.dosage.entity.DosageRecord;
import com.pill.platform.domain.dosage.entity.DosageSchedule;
import com.pill.platform.domain.dosage.entity.UserSupplement;
import com.pill.platform.domain.dosage.repository.DosageRecordRepository;
import com.pill.platform.domain.dosage.repository.DosageScheduleRepository;
import com.pill.platform.domain.dosage.repository.UserSupplementRepository;
import com.pill.platform.domain.supplement.repository.SupplementIngredientRepository;
import com.pill.platform.domain.user.entity.User;
import com.pill.platform.domain.user.repository.UserRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
public class DosageRecordService {

  private final DosageRecordRepository dosageRecordRepository;
  private final DosageScheduleRepository dosageScheduleRepository;
  private final UserSupplementRepository userSupplementRepository;
  private final SupplementIngredientRepository supplementIngredientRepository;
  private final UserRepository userRepository;
  private final TimingRuleService timingRuleService;

  @Transactional
  public DosageRecordResponse record(String email, DosageRecordRequest request) {
    UserSupplement us =
        userSupplementRepository
            .findById(request.userSupplementId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 복용 정보입니다."));
    if (!us.getUser().getId().equals(getUser(email).getId())) {
      throw new IllegalArgumentException("권한이 없습니다.");
    }

    DosageSchedule schedule = null;
    if (request.dosageScheduleId() != null) {
      schedule =
          dosageScheduleRepository
              .findById(request.dosageScheduleId())
              .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스케줄입니다."));
    }

    DosageRecord dr =
        DosageRecord.builder()
            .userSupplement(us)
            .dosageSchedule(schedule)
            .takenAt(request.takenAt())
            .isTaken(request.isTaken())
            .note(request.note())
            .build();
    return DosageRecordResponse.from(dosageRecordRepository.save(dr));
  }

  public List<DosageRecordResponse> getByDate(String email, LocalDate date) {
    User user = getUser(email);
    LocalDateTime from = date.atStartOfDay();
    LocalDateTime to = date.atTime(23, 59, 59);
    return dosageRecordRepository.findByUserIdAndTakenAtBetween(user.getId(), from, to).stream()
        .map(DosageRecordResponse::from)
        .toList();
  }

  public TodayChecklistResponse getToday(String email) {
    User user = getUser(email);
    LocalDate today = LocalDate.now();
    DayOfWeek dayOfWeek = today.getDayOfWeek();

    List<UserSupplement> activeSupplements =
        userSupplementRepository.findByUserIdAndIsActiveTrue(user.getId());

    List<DosageRecord> todayRecords =
        dosageRecordRepository.findByUserIdAndTakenAtBetween(
            user.getId(), today.atStartOfDay(), today.atTime(23, 59, 59));
    Set<Long> takenScheduleIds = new LinkedHashSet<>();
    for (DosageRecord dr : todayRecords) {
      if (Boolean.TRUE.equals(dr.getIsTaken()) && dr.getDosageSchedule() != null) {
        takenScheduleIds.add(dr.getDosageSchedule().getId());
      }
    }

    Set<String> allIngredients = new LinkedHashSet<>();
    List<Item> items = new ArrayList<>();

    for (UserSupplement us : activeSupplements) {
      supplementIngredientRepository
          .findBySupplementId(us.getSupplement().getId())
          .forEach(si -> allIngredients.add(si.getIngredient().getName()));

      for (DosageSchedule schedule :
          dosageScheduleRepository.findByUserSupplementIdAndIsActiveTrue(us.getId())) {
        if (!isScheduledToday(schedule, dayOfWeek)) continue;

        items.add(
            new Item(
                schedule.getId(),
                us.getId(),
                us.getSupplement().getProductName(),
                schedule.getScheduledTime(),
                timingRuleService.labelFor(schedule.getScheduledTime()),
                takenScheduleIds.contains(schedule.getId())));
      }
    }

    items.sort((a, b) -> a.scheduledTime().compareTo(b.scheduledTime()));

    String caution =
        allIngredients.contains("칼슘") && allIngredients.contains("철분")
            ? "철분은 칼슘과 함께 드시면 흡수를 방해해요. 철분은 오늘 저녁에 드세요."
            : null;

    return new TodayChecklistResponse(items, caution);
  }

  private boolean isScheduledToday(DosageSchedule schedule, DayOfWeek dayOfWeek) {
    return switch (dayOfWeek) {
      case MONDAY -> schedule.getMonday();
      case TUESDAY -> schedule.getTuesday();
      case WEDNESDAY -> schedule.getWednesday();
      case THURSDAY -> schedule.getThursday();
      case FRIDAY -> schedule.getFriday();
      case SATURDAY -> schedule.getSaturday();
      case SUNDAY -> schedule.getSunday();
    };
  }

  private User getUser(String email) {
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
  }
}
