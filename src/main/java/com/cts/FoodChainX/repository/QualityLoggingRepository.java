
package com.cts.FoodChainX.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cts.FoodChainX.model.QualityCheck;
@Repository
public interface QualityLoggingRepository extends JpaRepository<QualityCheck, Long> {
    List<QualityCheck> findByStatusIgnoreCase(String status);

    Optional<QualityCheck> findFirstByBatch_ProductionIdOrderByDateDesc(Long productionId);
}
