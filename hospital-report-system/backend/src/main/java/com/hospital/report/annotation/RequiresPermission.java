package com.hospital.report.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {
    
    String[] value() default {};
    
    Logical logical() default Logical.AND;
    
    enum Logical {
        AND, OR
    }
}