package Demo11;

/**
 * <p>
 * 定时要执行的任务
 * </p>
 *
 * @author Alemand
 * @since 2018/2/26
 */
public class Task implements Runnable {

    private int i;

    public Task(int i) {
        this.i = i;
    }

    @Override
    public void run() {
        System.out.println(i);
    }
}
