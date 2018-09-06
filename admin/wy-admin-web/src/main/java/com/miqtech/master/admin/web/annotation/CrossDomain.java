package com.miqtech.master.admin.web.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 添加跨域处理
 * @author 叶岸平
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface CrossDomain {
	boolean value() default false;
}
