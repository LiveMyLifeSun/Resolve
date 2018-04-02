package com.alemand.text;

import java.io.File;
import java.lang.reflect.Field;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2017/11/20
 */
public class AnnoatationProcessor {
    public static void getStudentInfo(Class<?> calzz){
        if(calzz.isAnnotationPresent(Persion.class)){
            Persion persion = calzz.getAnnotation(Persion.class);
            System.out.println(persion);
            System.out.println(persion.age());
            String[] hobby = persion.hobby();
            for (String str : hobby){
                System.out.println(str);
            }
        }
        Field[] fields = calzz.getDeclaredFields();
        for (Field field:fields) {
            System.out.println("fieldName=" + field.toString());
            if(field.isAnnotationPresent(StudentGender.class)){
                StudentGender annotation = field.getAnnotation(StudentGender.class);
                System.out.println(annotation);
                System.out.println(annotation.gender());
            }
        }

    }
}
