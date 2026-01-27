package com.service.shared.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify unique method code for logging
 * Each method should have a unique code for tracking
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MethodCode {
    /**
     * Unique code for the method (e.g., "CS001" for createSession)
     */
    String value();
    
    /**
     * Description of the method
     */
    String description() default "";
}
