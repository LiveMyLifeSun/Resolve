package com.alemand.spi.test;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2017/11/20
 */
public class INteg {
    public static void main(String[] args) {
        Integer n1 = new Integer(47);
        Integer n2 = new Integer(47);
        Integer n3 = 500;
        Integer n4 = 500;
        System.out.println(n1 == n2);
        System.out.println(n3 == n4);
        String a = "1";
        System.out.println(a + "2" + 3);
        System.out.println(a.equals("2")?5:10);
    }
}
