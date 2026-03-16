package com.cts.foodchainx.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cts.foodchainx.enums.CertificationStatus;
import com.cts.foodchainx.model.Farm;

/**
 * Repository interface for {@link Farm} entity.
 * Provides standard CRUD operations and custom query methods to access farm data 
 * from the database.
 */
@Repository
public interface FarmRepository extends JpaRepository<Farm, Long> {

    /**
     * Finds all farms belonging to a specific user.
     * <p>Uses JPA "Property Traversal" (Farmer_UserId) to join the Farm table 
     * with the User table and filter by the User's ID.</p>
     * * @param userId The unique identifier of the farmer.
     * @return A list of farms associated with the given user ID.
     */
    List<Farm> findByFarmer_UserId(Long userId);

    /**
     * Finds all farms based on their certification status, ignoring character case.
     * <p>Example: Searching for "approved" will return records marked as "APPROVED", 
     * "Approved", or "approved".</p>
     * * @param status The certification status to filter by (e.g., PENDING, APPROVED).
     * @return A list of farms matching the specified status.
     */
    List<Farm> findByCertificationStatus(CertificationStatus status);
}