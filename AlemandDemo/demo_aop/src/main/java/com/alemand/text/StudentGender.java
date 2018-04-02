package com.alemand.text;

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
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface StudentGender {

    //定义枚举
    public enum Gender{BOY ,GIRE};
    //使用枚举
    Gender gender() default Gender.BOY;
}
