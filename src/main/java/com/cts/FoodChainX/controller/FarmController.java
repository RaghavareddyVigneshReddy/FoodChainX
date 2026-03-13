package com.cts.FoodChainX.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication; // Added missing import
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cts.FoodChainX.dto.farm.FarmRequestDto;
import com.cts.FoodChainX.dto.farm.FarmResponseDto;
import com.cts.FoodChainX.service.FarmService;

import jakarta.validation.Valid;
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
           @Valid @RequestBody FarmRequestDto request, 
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
    @PreAuthorize("hasRole('REGULATOR')") // ONLY Regulators can call this!
    public ResponseEntity<FarmResponseDto> updateStatus(
            @PathVariable Long farmId, 
            @RequestParam String status) {
        log.info("Regulator updating Farm ID: {} to status: {}", farmId, status);
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