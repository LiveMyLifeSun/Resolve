package com.alemand.Demo;

import java.util.LinkedList;
import java.util.Queue;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2018/1/3
 */
public class QueueDemo {
    private Queue queue = new LinkedList();

    public Queue add(Command command){
        queue.offer(command);
        return queue;
    }

    public static void main(String[] args) {
        new Command();
    }
}
