
package com.cts.FoodChainX.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.cts.FoodChainX.model.QualityCheck;

@Repository
public interface QualityLoggingRepository extends JpaRepository<QualityCheck, Integer> {

}
