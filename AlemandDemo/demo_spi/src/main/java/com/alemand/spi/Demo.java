package com.alemand.spi;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2017/11/17
 */
public class Demo {
    public static void main(String[] args) {
        BigDecimal bigDecimal = new BigDecimal(0.15);
        BigDecimal bigDecimal1 = new BigDecimal(0.25);
        BigDecimal add = bigDecimal.add(bigDecimal);
        float v = add.floatValue();
        System.out.println(v);
        System.out.println("------------------------");
    }
}
