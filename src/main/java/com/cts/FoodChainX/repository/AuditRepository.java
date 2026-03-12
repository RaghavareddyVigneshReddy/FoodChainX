package com.cts.foodchainx.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cts.foodchainx.model.Audit;

import java.util.List;

@Repository
public interface AuditRepository extends JpaRepository<Audit, Long> {

    List<Audit> findByRegulatorId(Long regulatorId);

    List<Audit> findByStatus(String status);

    List<Audit> findByScope(String scope);
}