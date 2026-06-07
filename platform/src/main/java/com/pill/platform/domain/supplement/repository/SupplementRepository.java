package com.pill.platform.domain.supplement.repository;

import com.pill.platform.domain.supplement.entity.Supplement;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplementRepository extends JpaRepository<Supplement, Long> {

  List<Supplement> findByProductNameContainingIgnoreCase(String keyword);

  Optional<Supplement> findByApiProductId(String apiProductId);
}
