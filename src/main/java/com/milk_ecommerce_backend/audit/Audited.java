package com.milk_ecommerce_backend.audit;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Audited {
    String action();
    String entityType() default "";
    String entityIdParam() default "";
}