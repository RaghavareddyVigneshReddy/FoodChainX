package com.cts.foodchainx.repository;

import com.cts.foodchainx.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository interface for {@link Report} entities.
 * <p>
 * Provides standard CRUD operations and custom query methods to interact with 
 * the persistent storage of supply chain performance reports.
 * </p>
 *
 * @author FoodChainX Development Team
 * @version 1.0
 */
@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    /**
     * Retrieves a list of reports based on their scope, ignoring character casing.
     * <p>
     * Useful for fetching all reports filtered by specific boundaries such as 
     * 'GLOBAL', 'FARM', or 'RETAILER' without worrying about case sensitivity.
     * </p>
     *
     * @param scope the scope of the report to search for (e.g., "global")
     * @return a {@link List} of {@link Report} entities matching the specified scope
     */
    List<Report> findByScopeIgnoreCase(String scope);
}