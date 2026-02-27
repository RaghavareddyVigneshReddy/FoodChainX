package com.cts.FoodChainX.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cts.FoodChainX.model.Audit;

import java.util.List;

@Repository
public interface AuditRepository extends JpaRepository<Audit, Integer> {

    // Get all audits by Regulator ID
    List<Audit> findByRegulatorID(Integer regulatorID);

    // Get audits by status (OPEN / CLOSED)
    List<Audit> findByStatus(String status);

    // Get audits by scope (FARM / DISTRIBUTOR / RETAILER)
    List<Audit> findByScope(String scope);

}