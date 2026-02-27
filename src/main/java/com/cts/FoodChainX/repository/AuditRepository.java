package com.cts.FoodChainX.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cts.FoodChainX.model.Audit;

import java.util.List;

@Repository
public interface AuditRepository extends JpaRepository<Audit, Integer> {

    List<Audit> findByRegulatorID(Integer regulatorID);

    List<Audit> findByStatus(String status);

    List<Audit> findByScope(String scope);
}