package com.itjn.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @description 自定义注解，用于登录校验
 * @author JIU-W
 * @date 2025-01-11
 * @version 1.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})  //注解作用在方法或者类上
@Retention(RetentionPolicy.RUNTIME)              //注解在运行时存在
public @interface GlobalInterceptor {

    /**
     * 校验登录：默认不校验
     * @return
     */
    boolean checkLogin() default false;
}
