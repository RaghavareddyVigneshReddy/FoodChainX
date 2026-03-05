package com.cts.FoodChainX.controller;

import com.cts.FoodChainX.model.ComplianceRecord;
import com.cts.FoodChainX.service.ComplianceRecordService;

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
    public List<ComplianceRecord> getComplianceStatus(@PathVariable Integer entityId) {
        return complianceRecordService.getComplianceByEntity(entityId);
    }
}