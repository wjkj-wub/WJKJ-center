/**
 *
 */
package com.miqtech.master.admin.web.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target(METHOD)
/**
 * @author 张羽
 * 添加用户登录校验注解
 */
public @interface LoginValid {
	boolean valid() default false;
}
