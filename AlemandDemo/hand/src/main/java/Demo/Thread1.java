package Demo;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2017/12/8
 */
public class Thread1 implements Runnable {
    @Override
    public void run() {
        System.out.println("线程二工作了");
    }
}
