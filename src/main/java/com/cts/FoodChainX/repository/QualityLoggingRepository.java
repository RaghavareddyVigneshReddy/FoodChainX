
package com.cts.FoodChainX.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.cts.FoodChainX.model.QualityLogging;

@Repository
public interface QualityLoggingRepository extends JpaRepository<QualityLogging, Integer> {

}
