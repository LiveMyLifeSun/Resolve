package Demo13;

import java.lang.annotation.Annotation;

import Demo12.Demo;

/**
 * <p>
 * 类说明
 * </p>
 *
 * @author Alemand
 * @since 2018/3/21
 */
public class Test {
    public static void main(String[] args) {

        //获取B的class对象
        Class<B> bClass = B.class;
        //获取B类上的特定类型的注解如果不存在的话返回为null包括继承的注解
        Demo annotation = bClass.getAnnotation(Demo.class);
        //获取类上的所有的注解包括继承的注解
        Annotation[] annotations = bClass.getAnnotations();
        //也是获取注解但不包括继承的
        bClass.getDeclaredAnnotation(Demo.class);
        //也是获取注解但不包括继承的
        Annotation[] declaredAnnotations = bClass.getDeclaredAnnotations();
    }
}
