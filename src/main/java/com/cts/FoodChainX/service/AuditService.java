package com.cts.FoodChainX.service;

import com.cts.FoodChainX.model.Audit;
import com.cts.FoodChainX.repository.AuditRepository;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@Slf4j
public class AuditService {

    @Autowired
    private AuditRepository auditRepository;

    public Audit createAudit(Audit audit) {
        log.info("Creating new Audit record for Entity: {}", audit.getAuditId());
        audit.setDate(LocalDate.now());
        audit.setStatus("OPEN");
        Audit savedAudit = auditRepository.save(audit);
        log.debug("Audit record saved with ID: {}", savedAudit.getAuditId());
        return savedAudit;
    }

    public Audit closeAudit(Integer auditId) {

        Audit audit = auditRepository.findById(auditId)
                .orElseThrow(() -> new RuntimeException("Audit not found"));

        audit.setStatus("CLOSED");

        return auditRepository.save(audit);
    }
}