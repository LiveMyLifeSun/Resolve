package com.alemand.reflection;

import com.alemand.text.Apple;
import com.alemand.text.Student;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.lang.Character.getType;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2017/11/27
 */
public class Demo1 {
    public static void main(String[] args) throws IllegalAccessException {
        List<Integer> integers = Arrays.asList(1, 2, 3, 4, 5, 6);

        ArrayList<Integer> list = new ArrayList<Integer>();
        boolean b = list.addAll(integers);
        for(int i :list){
            System.out.println(i);
        }
    }
}
