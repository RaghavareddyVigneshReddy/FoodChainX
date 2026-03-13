package com.cts.foodchainx.aspect;

import java.lang.annotation.*;

import org.springframework.lang.NonNull;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {
    @NonNull String action();
    @NonNull String resource();
}
