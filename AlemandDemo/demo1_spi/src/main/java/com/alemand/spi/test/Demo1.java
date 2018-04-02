package com.alemand.spi.test;

/**
 * <p> 测试装饰者模式</p>
 *
 * @author Alemand
 * @since 2017/11/14
 */
public class Demo1 {
    public static void main(String[] args) {
        SuperMan superMan = new SuperMan(new Man());
        superMan.run();
    }
}
