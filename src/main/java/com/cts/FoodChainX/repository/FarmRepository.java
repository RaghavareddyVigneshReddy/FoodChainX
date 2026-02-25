package com.cts.FoodChainX.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.cts.FoodChainX.model.Farm;

@Repository
public interface FarmRepository extends JpaRepository<Farm, Integer> {
}
 

