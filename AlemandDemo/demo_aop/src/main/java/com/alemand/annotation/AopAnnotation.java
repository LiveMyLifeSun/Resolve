package com.alemand.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2017/11/20
 */
@Target(ElementType.METHOD)//用声明注解作用的位置
/**
 *用来描述自定义注解的生命周期有三种RetentionPoicy
 * 1.SOURCE:在原文件中有效
 * 2:ClASS:在class文件中有效
 * 3:RUNTIME:在运行是有效
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface AopAnnotation {
}
