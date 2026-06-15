package com.pill.platform.domain.dosage.service;

import com.pill.platform.domain.dosage.dto.DosageRecordRequest;
import com.pill.platform.domain.dosage.dto.DosageRecordResponse;
import com.pill.platform.domain.dosage.entity.DosageRecord;
import com.pill.platform.domain.dosage.entity.UserSupplement;
import com.pill.platform.domain.dosage.repository.DosageRecordRepository;
import com.pill.platform.domain.dosage.repository.UserSupplementRepository;
import com.pill.platform.domain.user.entity.User;
import com.pill.platform.domain.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DosageRecordService {

  private final DosageRecordRepository dosageRecordRepository;
  private final UserSupplementRepository userSupplementRepository;
  private final UserRepository userRepository;

  @Transactional
  public DosageRecordResponse record(String email, DosageRecordRequest request) {
    UserSupplement us =
        userSupplementRepository
            .findById(request.userSupplementId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 복용 정보입니다."));
    if (!us.getUser().getId().equals(getUser(email).getId())) {
      throw new IllegalArgumentException("권한이 없습니다.");
    }
    DosageRecord dr =
        DosageRecord.builder()
            .userSupplement(us)
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

  private User getUser(String email) {
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
  }
}
