package com.pill.platform.domain.user.repository;

import com.pill.platform.domain.user.entity.UserSymptom;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSymptomRepository extends JpaRepository<UserSymptom, Long> {

  List<UserSymptom> findByUserId(Long userId);

  void deleteByUserIdAndSymptomId(Long userId, Long symptomId);
}
