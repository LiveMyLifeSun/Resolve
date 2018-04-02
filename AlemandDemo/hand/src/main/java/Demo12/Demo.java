package Demo12;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import Demo7.Person;

/**
 * <p>
 * 类说明
 * </p>
 *
 * @author Alemand
 * @since 2018/3/16
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Repeatable(Demos.class)
public @interface Demo {

    /**
     *引用名value,因为没有设默认值所以在用注解的时候是必填字段,如果此注解
     * 只有这一个属性此时引用名可以省略
     */
    String value();
    /**
     *引用名为id因为有默认值所以在使用时可以不用给此属性赋值
     */
    String id() default "123";

}
