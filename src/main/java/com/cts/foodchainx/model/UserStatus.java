package com.cts.foodchainx.model;


/**
 * Represents the administrative status of a {@link User} account.
 */
public enum UserStatus {
  ACTIVE,    // User can login and perform actions
  INACTIVE,  // Account created but not yet verified or active
  SUSPENDED  // Account blocked by Admin due to policy violations
}