package com.cts.FoodChainX.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // Added missing import
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

    /**
     * POST: http://localhost:8081/api/farms/register
     * Securely registers a farm using the email from the JWT token.
     */
    @PostMapping("/register")
    public ResponseEntity<FarmResponseDto> registerFarm(
            @RequestBody FarmRequestDto request, 
            Authentication authentication) {
        
        // authentication.getName() returns the email/username set during JWT validation
        String email = authentication.getName(); 
        log.info("REST request to register new Farm for user: {}", email);
        
        return ResponseEntity.ok(farmService.creatingfarm(request, email));
    }

    /**
     * GET: http://localhost:8081/api/farms/my-farms
     * Returns all farms belonging to the currently logged-in user.
     */
    @GetMapping("/my-farms")
    public ResponseEntity<List<FarmResponseDto>> getMyFarms(Authentication authentication) {
        String email = authentication.getName();
        log.info("Fetching farms for logged-in user: {}", email);
        
        return ResponseEntity.ok(farmService.getAllFarmsByFarmerEmail(email));
    }

    /**
     * PATCH: http://localhost:8081/api/farms/{farmId}/status?status=CERTIFIED
     * Typically used by a REGULATOR to update certification.
     */
    @PatchMapping("/{farmId}/status")
    public ResponseEntity<FarmResponseDto> updateStatus(
            @PathVariable Long farmId, 
            @RequestParam String status) {
        log.warn("REST request to UPDATE Farm Status for Farm ID: {} to {}", farmId, status);
        return ResponseEntity.ok(farmService.updateStatus(farmId, status));
    }

    /**
     * DELETE: http://localhost:8081/api/farms/{farmId}
     */
    @DeleteMapping("/{farmId}")
    public ResponseEntity<String> removeFarm(@PathVariable Long farmId, Authentication authentication) {
        String email = authentication.getName();
        log.warn("REST request to DELETE Farm ID: {}", farmId);
        return ResponseEntity.ok(farmService.deleteFarm(farmId, email));
    }
}