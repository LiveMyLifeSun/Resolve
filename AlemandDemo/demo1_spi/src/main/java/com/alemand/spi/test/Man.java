package com.alemand.spi.test;

/**
 * <p> 装饰者模式的模仿 </p>
 *
 * @author Alemand
 * @since 2017/11/14
 */
public class Man implements People {
    @Override
    public void run() {
        System.out.println("人会跑");
    }
}
