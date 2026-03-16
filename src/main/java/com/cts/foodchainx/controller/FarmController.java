package com.cts.foodchainx.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cts.foodchainx.dto.farm.FarmRequestDto;
import com.cts.foodchainx.dto.farm.FarmResponseDto;
import com.cts.foodchainx.enums.CertificationStatus;
import com.cts.foodchainx.service.FarmService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for managing Farm-related operations in the food supply chain.
 * Provides endpoints for registration, retrieval, status updates, and deletion of farms.
 * * @author Your Name/Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/farms")
@Slf4j
@RequiredArgsConstructor
public class FarmController {

    private final FarmService farmService;

    /**
     * Registers a new farm in the system.
     * Endpoint: POST /api/farms/register
     * * @param request The farm details (name, location, etc.) provided in the request body.
     * @param authentication The current security context containing the user's credentials.
     * @return ResponseEntity containing the created FarmResponseDto and HTTP status 200 OK.
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
     * Retrieves a list of all farms owned by the currently authenticated user.
     * Endpoint: GET /api/farms/my-farms
     * * @param authentication The current security context used to identify the user's email.
     * @return ResponseEntity containing a list of FarmResponseDto objects and HTTP status 200 OK.
     */
    @GetMapping("/my-farms")
    public ResponseEntity<List<FarmResponseDto>> getMyFarms(Authentication authentication) {
        String email = authentication.getName();
        log.info("Fetching farms for logged-in user: {}", email);
        
        return ResponseEntity.ok(farmService.getAllFarmsByFarmerEmail(email));
    }

    /**
     * Updates the certification or operational status of a specific farm.
     * Access is restricted to users with the 'REGULATOR' role.
     * Endpoint: PATCH /api/farms/{farmId}/status?status=VALUE
     * * @param farmId The unique identifier of the farm to update.
     * @param status The new status string to apply (e.g., CERTIFIED, SUSPENDED).
     * @return ResponseEntity containing the updated FarmResponseDto and HTTP status 200 OK.
     */
    @PatchMapping("/{farmId}/status")
    @PreAuthorize("hasRole('REGULATOR')") // ONLY Regulators can call this!
    public ResponseEntity<FarmResponseDto> updateStatus(
            @PathVariable @NonNull Long farmId, 
            @RequestParam CertificationStatus status) {
        log.info("Regulator updating Farm ID: {} to status: {}", farmId, status);
        return ResponseEntity.ok(farmService.updateStatus(farmId, status));
    }

    /**
     * Removes a farm from the system.
     * Endpoint: DELETE /api/farms/{farmId}
     * * @param farmId The unique identifier of the farm to be deleted.
     * @param authentication The current security context used to verify ownership or permissions.
     * @return ResponseEntity containing a confirmation message and HTTP status 200 OK.
     */
    @DeleteMapping("/{farmId}")
    public ResponseEntity<String> removeFarm(@PathVariable @NonNull Long farmId, Authentication authentication) {
        String email = authentication.getName();
        log.warn("REST request to DELETE Farm ID: {}", farmId);
        return ResponseEntity.ok(farmService.deleteFarm(farmId, email));
    }
}