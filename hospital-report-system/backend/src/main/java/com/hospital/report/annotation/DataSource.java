package com.hospital.report.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DataSource {
    
    String value() default "default";
    
    enum Type {
        READ, WRITE, READ_WRITE
    }
    
    Type type() default Type.READ_WRITE;
}