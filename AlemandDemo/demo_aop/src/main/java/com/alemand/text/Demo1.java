package com.alemand.text;



import java.lang.reflect.Field;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2017/11/27
 */
public class Demo1 {
    public static void main(String[] args) {
        Apple apple = new Apple();
        apple.getClass();
        Class<Apple> appleClass = Apple.class;
    }
}
