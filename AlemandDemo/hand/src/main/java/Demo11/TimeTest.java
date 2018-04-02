package Demo11;

import java.util.TimerTask;

/**
 * <p>
 * 创建一个类继承TimerTask重写run方法
 * </p>
 *
 * @author Alemand
 * @since 2018/2/26
 */
public class TimeTest extends TimerTask {

    private int i;

    public TimeTest(int i) {
        this.i = i;
    }

    /**
     * 重写自Timertask,run方法执行的就是具体的定时任务
     */
    @Override
    public void run() {
        System.out.println(i);
    }
}
