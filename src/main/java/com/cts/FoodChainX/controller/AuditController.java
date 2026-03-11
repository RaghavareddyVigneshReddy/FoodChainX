package com.cts.FoodChainX.controller;

import com.cts.FoodChainX.model.Audit;
import com.cts.FoodChainX.service.AuditService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/compliance/audits")
public class AuditController {

    @Autowired
    private AuditService auditService;

    @PostMapping
    public Audit createAudit(@RequestBody Audit audit) {
        return auditService.createAudit(audit);
    }

    @PutMapping("/{id}/close")
    public Audit closeAudit(@PathVariable Long id) {
        return auditService.closeAudit(id);
    }
}