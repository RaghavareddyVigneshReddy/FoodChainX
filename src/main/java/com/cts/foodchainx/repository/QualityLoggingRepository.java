
package com.cts.foodchainx.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cts.foodchainx.model.QualityCheck;
@Repository
public interface QualityLoggingRepository extends JpaRepository<QualityCheck, Long> {
    List<QualityCheck> findByStatusIgnoreCase(String status);

    Optional<QualityCheck> findFirstByBatch_ProductionIdOrderByDateDesc(Long productionId);
}
