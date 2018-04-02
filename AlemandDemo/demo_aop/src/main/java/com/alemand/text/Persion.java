package com.alemand.text;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2017/11/20
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Persion {
    String name() default "zcx";
    int age() default 18;

    String[] hobby() default {"basketball","football"};
}
