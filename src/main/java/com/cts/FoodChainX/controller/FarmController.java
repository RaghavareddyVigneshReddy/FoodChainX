package com.cts.FoodChainX.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cts.FoodChainX.dto.farm.FarmRequestDto;
import com.cts.FoodChainX.dto.farm.FarmResponseDto;
import com.cts.FoodChainX.service.FarmService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/farms")
@Slf4j
public class FarmController {

    @Autowired
    private FarmService farmService;

    // POST: http://localhost:8080/api/farms/register/1
    @PostMapping("/register/{farmerId}")
    public ResponseEntity<FarmResponseDto> registerFarm(
            @RequestBody FarmRequestDto request, 
            @PathVariable Long farmerId) {
        log.info("REST request to register new Farm for Farmer ID: {}", farmerId);
        return ResponseEntity.ok(farmService.creatingfarm(request, farmerId));
    }

    // GET: http://localhost:8080/api/farms/farmer/1
    @GetMapping("/farmer/{farmerId}")
    public ResponseEntity<List<FarmResponseDto>> getMyFarms(@PathVariable Long farmerId) {
        return ResponseEntity.ok(farmService.getAllFarmsByFarmer(farmerId));
    }

    // PATCH: http://localhost:8080/api/farms/1/status?status=CERTIFIED
    @PatchMapping("/{farmId}/status")
    public ResponseEntity<FarmResponseDto> updateStatus(
            @PathVariable Long farmId, 
            @RequestParam String status) {
        log.warn("REST request to UPDATE Farm Status for Farm ID: {}", farmId);
        return ResponseEntity.ok(farmService.updateStatus(farmId, status));
    }

    // DELETE: http://localhost:8080/api/farms/1
    @DeleteMapping("/{farmId}")
    public ResponseEntity<String> removeFarm(@PathVariable Long farmId) {
        log.warn("REST request to DELETE Farm ID: {}", farmId);
        return ResponseEntity.ok(farmService.deleteFarm(farmId));
    }
}
