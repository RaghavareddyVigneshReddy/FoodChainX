package com.cts.foodchainx.controller;

import com.cts.foodchainx.model.ComplianceRecord;
import com.cts.foodchainx.service.ComplianceRecordService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compliance")
public class ComplianceRecordController {

    @Autowired
    private ComplianceRecordService complianceRecordService;

    @PostMapping("/records")
    public ComplianceRecord createComplianceRecord(@RequestBody ComplianceRecord record) {
        return complianceRecordService.createComplianceRecord(record);
    }

    @GetMapping("/status/{entityId}")
    public List<ComplianceRecord> getComplianceStatus(@PathVariable Long entityId) {
        return complianceRecordService.getComplianceByEntity(entityId);
    }
}