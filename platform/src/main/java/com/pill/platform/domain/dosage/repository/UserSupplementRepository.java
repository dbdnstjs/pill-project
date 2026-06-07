package com.pill.platform.domain.dosage.repository;

import com.pill.platform.domain.dosage.entity.UserSupplement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSupplementRepository extends JpaRepository<UserSupplement, Long> {

  List<UserSupplement> findByUserIdAndIsActiveTrue(Long userId);
}
