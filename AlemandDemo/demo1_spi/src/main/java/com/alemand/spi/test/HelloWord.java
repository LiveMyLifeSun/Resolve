package com.alemand.spi.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2017/11/20
 */
public class HelloWord {
    public static void main(String[] args) {
        Number n2 = new Number();
        Number n1 = new Number();
        n1.i=10;
        n2.i= 20;
        System.out.println("n1:"+n1.i );
        System.out.println("n2:"+n2.i);
        n1=n2;
        System.out.println("n1:"+n1.i );
        System.out.println("n2:"+n2.i);
        n1.i=30;
        System.out.println("n1:"+n1.i );
        System.out.println("n2:"+n2.i);
        System.exit(0);

    }
}
