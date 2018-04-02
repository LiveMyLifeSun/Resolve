package com.alemand.spi.test;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2017/11/14
 */
public class Decorator implements People {

    private People people;

    public Decorator(People people){
        this.people = people;
    }


    public void run() {
        people.run();
    }
}
