package com.alemand.spi.test;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2017/11/14
 */
public class SuperMan extends Decorator{

    public SuperMan(People people) {
        super(people);
    }

    @Override
    public void run() {
        super.run();
        System.out.println("超人会飞");
    }
}
