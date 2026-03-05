package com.cts.FoodChainX.service;

import com.cts.FoodChainX.model.Audit;
import com.cts.FoodChainX.repository.AuditRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class AuditService {

    @Autowired
    private AuditRepository auditRepository;

    public Audit createAudit(Audit audit) {

        audit.setDate(LocalDate.now());
        audit.setStatus("OPEN");

        return auditRepository.save(audit);
    }

    public Audit closeAudit(Integer auditId) {

        Audit audit = auditRepository.findById(auditId)
                .orElseThrow(() -> new RuntimeException("Audit not found"));

        audit.setStatus("CLOSED");

        return auditRepository.save(audit);
    }
}