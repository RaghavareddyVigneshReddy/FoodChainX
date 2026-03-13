package com.cts.foodchainx.controller;

import com.cts.foodchainx.model.Audit;
import com.cts.foodchainx.service.AuditService;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/compliance/audits")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @PostMapping
    public Audit createAudit(@RequestBody Audit audit) {
        return auditService.createAudit(audit);
    }

    @PutMapping("/{id}/close")
    public Audit closeAudit(@PathVariable @NonNull Long id) {
        return auditService.closeAudit(id);
    }
}