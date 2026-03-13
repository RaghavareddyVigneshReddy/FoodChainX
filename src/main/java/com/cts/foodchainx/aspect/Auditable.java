package com.cts.foodchainx.aspect;

import java.lang.annotation.*;
import org.springframework.lang.NonNull;

/**
 * Custom annotation used to mark service methods that require audit logging.
 * <p>
 * This annotation is intercepted by the {@link AuditAspect} to capture 
 * user actions and resource modifications within the FoodChainX platform.
 * </p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {

    /**
     * The specific operation being performed.
     * @return a string representing the action (e.g., "CREATE_FARM", "DELETE_BATCH")
     */
    @NonNull String action();

    /**
     * The type of resource affected by this action.
     * @return a string representing the resource (e.g., "FARM", "SHIPMENT")
     */
    @NonNull String resource();
}