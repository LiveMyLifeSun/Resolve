package Demo;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2017/12/8
 */
public class Demo4 {
    public static void main(String[] args) {
        SingletetonThread singletetonThread = new SingletetonThread();
        singletetonThread.run();
        Thread1 thread1 = new Thread1();
        thread1.run();
    }
}
