package com.example.backend.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法级权限校验注解。
 *
 * <p>标注在 Controller 方法上，value 为所需权限标识（permKey），例如
 * {@code @RequirePermission("doc:upload")}，由 {@link PermissionAspect} 在方法执行前校验。</p>
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /** 所需权限标识（permKey），如 doc:upload、sys:user:manage */
    String value();
}