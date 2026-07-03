package com.pill.platform.domain.user.repository;

import com.pill.platform.domain.user.entity.UserSymptom;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface UserSymptomRepository extends JpaRepository<UserSymptom, Long> {

  List<UserSymptom> findByUserId(Long userId);

  @Modifying
  @Transactional
  void deleteByUserIdAndSymptomId(Long userId, Long symptomId);
}
