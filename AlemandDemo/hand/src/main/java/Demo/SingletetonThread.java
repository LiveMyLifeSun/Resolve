package Demo;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2017/12/7
 */
public class SingletetonThread implements Runnable {
    @Override
    public void run() {
        System.out.println("线程一工作了");
    }
}
